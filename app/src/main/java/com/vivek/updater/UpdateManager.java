package com.vivek.updater;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpdateManager {

    private static final String PREFS_NAME = "vivek_updater_prefs";
    private static final String KEY_LAST_CHECK_TIME = "last_check_time";
    private static final String KEY_LAST_CHECKED_VERSION = "last_checked_version";
    private static final String KEY_FAILED_VERSION = "failed_version";
    private static final String KEY_FAILED_COUNT = "failed_count";
    private static final String KEY_ACTIVE_SESSION_ID = "active_session_id";
    private static final String KEY_PERMISSION_ASKED = "permission_asked";

    private static volatile UpdateManager sInstance;

    private final Context mContext;
    private final SharedPreferences mPrefs;
    private final AtomicBoolean mIsUpdating = new AtomicBoolean(false);

    private UpdateManager(Context context) {
        mContext = context.getApplicationContext();
        mPrefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static UpdateManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (UpdateManager.class) {
                if (sInstance == null) {
                    sInstance = new UpdateManager(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * Check if "Install Unknown Apps" permission is granted on Android 8.0+.
     */
    public boolean canRequestPackageInstalls() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return mContext.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    /**
     * Prompts the user ONCE to grant "Install Unknown Apps" permission if not already granted.
     * Never asks repeatedly.
     */
    public void promptInstallUnknownAppsOnce(Activity activity, int requestCode) {
        if (activity == null || activity.isFinishing()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!canRequestPackageInstalls()) {
                boolean alreadyAsked = mPrefs.getBoolean(KEY_PERMISSION_ASKED, false);
                if (!alreadyAsked) {
                    mPrefs.edit().putBoolean(KEY_PERMISSION_ASKED, true).apply();
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivityForResult(intent, requestCode);
                    } catch (Exception e) {
                        UpdateLogger.e("Failed to open unknown app settings", e);
                    }
                }
            }
        }
    }

    /**
     * Trigger asynchronous update check (e.g. on app launch or network state change).
     */
    public void checkForUpdate(boolean silent) {
        new Thread(() -> checkForUpdateSync(silent)).start();
    }

    /**
     * Synchronous update check suitable for WorkManager or background thread.
     */
    public boolean checkForUpdateSync(boolean silent) {
        if (!mIsUpdating.compareAndSet(false, true)) {
            UpdateLogger.i("Update check already in progress. Skipping duplicate.");
            return false;
        }

        try {
            UpdateLogger.log(UpdateLogger.AUTO_UPDATE_CHECK, "Starting update check...");

            UpdateConfig config = fetchRemoteConfig();
            if (config == null) {
                UpdateLogger.w("Could not fetch or parse update config.");
                return false;
            }

            long installedVersion = ApkVerifier.getInstalledVersionCode(mContext);
            mPrefs.edit()
                    .putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis())
                    .putLong(KEY_LAST_CHECKED_VERSION, config.versionCode)
                    .apply();

            if (config.versionCode <= installedVersion) {
                UpdateLogger.i("App is up-to-date (installed: " + installedVersion + ", remote: " + config.versionCode + ")");
                cleanUpdatesDir();
                return true;
            }

            UpdateLogger.log(UpdateLogger.UPDATE_AVAILABLE,
                    "New update found! Installed: " + installedVersion + ", Remote: " + config.versionCode);

            // Prevent rapid failure loops: if the same version failed repeatedly (> 3 times), cool off for 1 hour
            long lastFailedVersion = mPrefs.getLong(KEY_FAILED_VERSION, -1);
            int failedCount = mPrefs.getInt(KEY_FAILED_COUNT, 0);
            if (lastFailedVersion == config.versionCode && failedCount >= 3) {
                long lastCheck = mPrefs.getLong(KEY_LAST_CHECK_TIME, 0);
                if (System.currentTimeMillis() - lastCheck < 3600000L) {
                    UpdateLogger.w("Update cooled off due to repeated failures (" + failedCount + " times).");
                    return false;
                }
            }

            // Download APK into safe app-private cache
            File downloadedApk = downloadApk(config.apkUrl);
            if (downloadedApk == null || !downloadedApk.exists()) {
                recordFailure(config.versionCode, "Download failed");
                return false;
            }

            // 8-Point Cryptographic & Integrity Verification
            ApkVerifier.VerificationResult verification = ApkVerifier.verifyApk(
                    mContext, downloadedApk, config.apkUrl, config.sha256);

            if (!verification.isValid) {
                recordFailure(config.versionCode, "Verification failed: " + verification.errorMessage);
                return false;
            }

            // Before committing installation, compare candidate version again against current installed version
            long freshInstalledVersion = ApkVerifier.getInstalledVersionCode(mContext);
            if (verification.archiveVersionCode <= freshInstalledVersion) {
                UpdateLogger.w("Candidate version is no longer newer than installed version. Aborting.");
                ApkVerifier.deleteFile(downloadedApk);
                return false;
            }

            // Install update via official PackageInstaller Session with Intent fallback
            boolean sessionSuccess = installPackageSession(downloadedApk);
            if (!sessionSuccess) {
                UpdateLogger.i("PackageInstaller session unsuccessful, triggering fallback Intent install...");
                return installPackageViaIntent(downloadedApk);
            }
            return true;

        } catch (Exception e) {
            UpdateLogger.e("Exception during update check: " + e.getMessage(), e);
            return false;
        } finally {
            mIsUpdating.set(false);
        }
    }

    private File downloadApk(String apkUrl) {
        if (apkUrl == null || apkUrl.trim().isEmpty() || !apkUrl.toLowerCase().startsWith("https://")) {
            UpdateLogger.w("Invalid or non-HTTPS APK URL: " + apkUrl);
            return null;
        }

        UpdateLogger.log(UpdateLogger.DOWNLOAD_STARTED, "Downloading APK from: " + apkUrl);

        File updatesDir = new File(mContext.getCacheDir(), "updates");
        if (!updatesDir.exists()) updatesDir.mkdirs();

        File tempFile = new File(updatesDir, "pending_update.apk.tmp");
        File finalFile = new File(updatesDir, "pending_update.apk");

        if (tempFile.exists()) tempFile.delete();
        if (finalFile.exists()) finalFile.delete();

        HttpURLConnection conn = null;
        InputStream in = null;
        FileOutputStream out = null;

        try {
            String currentUrl = apkUrl;
            if (currentUrl.contains("?")) {
                currentUrl += "&_t=" + System.currentTimeMillis();
            } else {
                currentUrl += "?_t=" + System.currentTimeMillis();
            }

            int redirects = 0;
            while (redirects < 10) {
                URL url = new URL(currentUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
                conn.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                conn.setRequestProperty("Pragma", "no-cache");
                conn.setRequestProperty("Accept", "*/*");

                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER
                        || status == 307 || status == 308) {
                    String redirectUrl = conn.getHeaderField("Location");
                    if (redirectUrl != null && !redirectUrl.isEmpty()) {
                        currentUrl = redirectUrl;
                        redirects++;
                        conn.disconnect();
                        continue;
                    }
                }

                if (status != HttpURLConnection.HTTP_OK) {
                    UpdateLogger.w("HTTP download error code: " + status);
                    return null;
                }
                break;
            }

            in = conn.getInputStream();
            out = new FileOutputStream(tempFile);

            byte[] buffer = new byte[65536];
            int read;
            long totalRead = 0;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                totalRead += read;
            }
            out.flush();

            if (totalRead < 1024) {
                UpdateLogger.w("Downloaded file too small: " + totalRead + " bytes");
                ApkVerifier.deleteFile(tempFile);
                return null;
            }

            if (!tempFile.renameTo(finalFile)) {
                UpdateLogger.w("Failed to atomically rename temp APK to final destination");
                return null;
            }

            UpdateLogger.log(UpdateLogger.DOWNLOAD_COMPLETE, "Downloaded " + totalRead + " bytes to " + finalFile.getAbsolutePath());
            return finalFile;

        } catch (Exception e) {
            UpdateLogger.e("Download interrupted or failed: " + e.getMessage(), e);
            ApkVerifier.deleteFile(tempFile);
            ApkVerifier.deleteFile(finalFile);
            return null;
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (conn != null) conn.disconnect();
            } catch (Exception ignored) {}
        }
    }

    private boolean installPackageSession(File apkFile) {
        if (apkFile == null || !apkFile.exists()) return false;

        PackageManager pm = mContext.getPackageManager();
        PackageInstaller installer = pm.getPackageInstaller();

        // Abandon any existing stale sessions created by this app
        try {
            List<PackageInstaller.SessionInfo> mySessions = installer.getMySessions();
            for (PackageInstaller.SessionInfo info : mySessions) {
                try {
                    installer.abandonSession(info.getSessionId());
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        PackageInstaller.Session session = null;
        int sessionId = -1;

        try {
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);

            // Dynamic targeting of current package
            params.setAppPackageName(mContext.getPackageName());
            params.setSize(apkFile.length());

            // Android 12+ (API 31+): setRequireUserAction(USER_ACTION_NOT_REQUIRED)
            if (Build.VERSION.SDK_INT >= 31) {
                try {
                    Method setRequireUserAction = params.getClass().getMethod("setRequireUserAction", int.class);
                    // SessionParams.USER_ACTION_NOT_REQUIRED = 2
                    setRequireUserAction.invoke(params, 2);
                    UpdateLogger.log(UpdateLogger.SILENT_INSTALL_REQUESTED,
                            "Configured SessionParams.USER_ACTION_NOT_REQUIRED (API " + Build.VERSION.SDK_INT + ")");
                } catch (Exception e) {
                    UpdateLogger.w("Failed to setRequireUserAction: " + e.getMessage());
                }
            }

            // Android 14+ (API 34+): setPackageSource(PACKAGE_SOURCE_OTHER = 1)
            if (Build.VERSION.SDK_INT >= 34) {
                try {
                    Method setPackageSource = params.getClass().getMethod("setPackageSource", int.class);
                    setPackageSource.invoke(params, 1);
                } catch (Exception ignored) {}
            }

            // Set fast install scenario
            try {
                // PackageManager.INSTALL_SCENARIO_FAST = 1
                Method setInstallScenario = params.getClass().getMethod("setInstallScenario", int.class);
                setInstallScenario.invoke(params, 1);
            } catch (Exception ignored) {}

            sessionId = installer.createSession(params);
            mPrefs.edit().putInt(KEY_ACTIVE_SESSION_ID, sessionId).apply();
            UpdateLogger.log(UpdateLogger.INSTALL_SESSION_CREATED, "Created PackageInstaller Session ID: " + sessionId);

            session = installer.openSession(sessionId);

            try (InputStream in = new FileInputStream(apkFile);
                 OutputStream out = session.openWrite("vivek_panel_update", 0, apkFile.length())) {
                byte[] buffer = new byte[65536];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                session.fsync(out);
            }

            Intent callbackIntent = new Intent(mContext, UpdateInstallReceiver.class);
            callbackIntent.setAction(UpdateInstallReceiver.ACTION_INSTALL_STATUS);
            callbackIntent.setPackage(mContext.getPackageName());

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 31) {
                // PendingIntent.FLAG_MUTABLE = 0x02000000 (API 31+)
                flags |= 0x02000000;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    mContext,
                    sessionId,
                    callbackIntent,
                    flags
            );

            session.commit(pendingIntent.getIntentSender());
            UpdateLogger.i("Session committed to PackageInstaller successfully.");
            return true;

        } catch (Exception e) {
            UpdateLogger.e("Failed to create or commit PackageInstaller session: " + e.getMessage(), e);
            if (sessionId != -1) {
                try {
                    installer.abandonSession(sessionId);
                } catch (Exception ignored) {}
            }
            recordFailure(-1, "Session commit failed: " + e.getMessage());
            return false;
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception ignored) {}
            }
        }
    }

    public boolean installPackageViaIntent(File apkFile) {
        if (apkFile == null || !apkFile.exists()) return false;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apkUri = androidx.core.content.FileProvider.getUriForFile(
                        mContext,
                        mContext.getPackageName() + ".provider",
                        apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = Uri.fromFile(apkFile);
            }
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            mContext.startActivity(intent);
            UpdateLogger.i("Launched fallback package install intent via FileProvider.");
            return true;
        } catch (Exception e) {
            UpdateLogger.e("Fallback intent install failed: " + e.getMessage(), e);
            recordFailure(-1, "Fallback intent install failed: " + e.getMessage());
            return false;
        }
    }

    public void onInstallSuccess() {
        cleanUpdatesDir();
        mPrefs.edit()
                .remove(KEY_FAILED_VERSION)
                .remove(KEY_FAILED_COUNT)
                .remove(KEY_ACTIVE_SESSION_ID)
                .apply();
        UpdateLogger.i("Install success cleanup complete.");
    }

    public void onInstallFailed(int status, String message) {
        cleanUpdatesDir();
        int currentFailed = mPrefs.getInt(KEY_FAILED_COUNT, 0);
        mPrefs.edit()
                .putInt(KEY_FAILED_COUNT, currentFailed + 1)
                .remove(KEY_ACTIVE_SESSION_ID)
                .apply();
        UpdateLogger.w("Install failed [status=" + status + "]: " + message);
    }

    private void recordFailure(long versionCode, String reason) {
        UpdateLogger.w("Update aborted/failed: " + reason);
        int count = mPrefs.getInt(KEY_FAILED_COUNT, 0);
        mPrefs.edit()
                .putLong(KEY_FAILED_VERSION, versionCode)
                .putInt(KEY_FAILED_COUNT, count + 1)
                .apply();
    }

    private void cleanUpdatesDir() {
        try {
            File updatesDir = new File(mContext.getCacheDir(), "updates");
            if (updatesDir.exists() && updatesDir.isDirectory()) {
                File[] files = updatesDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private UpdateConfig fetchRemoteConfig() {
        String[] urlsToTry = new String[] {
                "https://raw.githubusercontent.com/ASHU0098482/vivekpanel/HEAD/config.json?t=" + System.currentTimeMillis() + "&rnd=" + (int)(Math.random() * 1000000),
                "https://raw.githubusercontent.com/ASHU0098482/vivekpanel/HEAD/config.json",
                "https://raw.githubusercontent.com/ASHU0098482/vivekpanel/main/config.json?t=" + System.currentTimeMillis() + "&rnd=" + (int)(Math.random() * 1000000),
                "https://raw.githubusercontent.com/ASHU0098482/vivekpanel/main/config.json"
        };

        for (String urlStr : urlsToTry) {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setUseCaches(false);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36");
                conn.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    JSONObject json = new JSONObject(sb.toString().trim());

                    long versionCode = json.optLong("versionCode", json.optLong("apk_version_code", -1));
                    String apkUrl = json.optString("apkUrl", json.optString("apk_update_url", ""));
                    String sha256 = json.optString("sha256", json.optString("apk_sha256", ""));
                    String versionName = json.optString("versionName", json.optString("apk_version_name", ""));
                    boolean forceUpdate = json.optBoolean("forceUpdate", json.optBoolean("force_update", false));
                    String releaseNotes = json.optString("releaseNotes", json.optString("release_notes", ""));

                    if (versionCode > 0 && !apkUrl.isEmpty()) {
                        return new UpdateConfig(versionCode, versionName, apkUrl, sha256, forceUpdate, releaseNotes);
                    }
                }
            } catch (Exception e) {
                UpdateLogger.w("Failed to fetch config from: " + urlStr + " (" + e.getMessage() + ")");
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (conn != null) conn.disconnect();
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    public static class UpdateConfig {
        public final long versionCode;
        public final String versionName;
        public final String apkUrl;
        public final String sha256;
        public final boolean forceUpdate;
        public final String releaseNotes;

        public UpdateConfig(long versionCode, String versionName, String apkUrl, String sha256, boolean forceUpdate, String releaseNotes) {
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.apkUrl = apkUrl;
            this.sha256 = sha256;
            this.forceUpdate = forceUpdate;
            this.releaseNotes = releaseNotes;
        }
    }
}
