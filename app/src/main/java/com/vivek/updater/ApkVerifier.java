package com.vivek.updater;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ApkVerifier {

    private ApkVerifier() {}

    public static class VerificationResult {
        public final boolean isValid;
        public final String errorMessage;
        public final long archiveVersionCode;

        public VerificationResult(boolean isValid, String errorMessage, long archiveVersionCode) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            this.archiveVersionCode = archiveVersionCode;
        }

        public static VerificationResult success(long versionCode) {
            return new VerificationResult(true, null, versionCode);
        }

        public static VerificationResult failure(String message) {
            return new VerificationResult(false, message, -1);
        }
    }

    /**
     * Verifies the candidate APK file against the currently installed application
     * and security constraints. If verification fails, deletes the file.
     */
    public static VerificationResult verifyApk(
            Context context,
            File apkFile,
            String downloadUrl,
            String expectedSha256) {

        if (apkFile == null || !apkFile.exists() || apkFile.length() < 1024) {
            deleteFile(apkFile);
            return VerificationResult.failure("APK file does not exist or is empty");
        }

        // 1. Enforce HTTPS URL
        if (downloadUrl != null && !downloadUrl.trim().toLowerCase().startsWith("https://")) {
            deleteFile(apkFile);
            return VerificationResult.failure("Insecure download URL (HTTPS required): " + downloadUrl);
        }

        // 2. Validate SHA-256 if provided
        if (expectedSha256 != null && !expectedSha256.trim().isEmpty()) {
            String actualSha256 = calculateFileSha256(apkFile);
            if (actualSha256 == null || !actualSha256.equalsIgnoreCase(expectedSha256.trim())) {
                deleteFile(apkFile);
                return VerificationResult.failure("SHA-256 mismatch! Expected: " + expectedSha256 + ", Got: " + actualSha256);
            }
            UpdateLogger.log(UpdateLogger.APK_VERIFIED, "SHA-256 verified successfully: " + actualSha256);
        }

        PackageManager pm = context.getPackageManager();
        String currentPackageName = context.getPackageName();

        // 3. Verify file is a valid APK and can be parsed by PackageManager
        PackageInfo archiveInfo = null;
        try {
            int flags = PackageManager.GET_SIGNATURES;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                flags |= PackageManager.GET_SIGNING_CERTIFICATES;
            }
            archiveInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), flags);
        } catch (Exception e) {
            UpdateLogger.e("Failed to parse APK archive info", e);
        }

        if (archiveInfo == null) {
            deleteFile(apkFile);
            return VerificationResult.failure("Corrupted APK file: PackageParser failed to parse archive");
        }

        // 4. Verify Package Name
        if (!currentPackageName.equals(archiveInfo.packageName)) {
            deleteFile(apkFile);
            return VerificationResult.failure("Package name mismatch! Expected: " + currentPackageName + ", Found: " + archiveInfo.packageName);
        }

        // 5. Verify Version Code is strictly greater than installed version
        long installedVersionCode = getInstalledVersionCode(context);
        long candidateVersionCode = getArchiveVersionCode(archiveInfo);

        if (candidateVersionCode <= installedVersionCode) {
            deleteFile(apkFile);
            return VerificationResult.failure("Candidate version (" + candidateVersionCode + ") is not newer than installed version (" + installedVersionCode + ")");
        }

        // 6. Verify Signing Certificate matches currently installed application
        boolean certsMatch = verifySigningCertificates(context, archiveInfo);
        if (!certsMatch) {
            deleteFile(apkFile);
            return VerificationResult.failure("Signing certificate mismatch! APK is signed with a different key.");
        }

        UpdateLogger.log(UpdateLogger.APK_VERIFIED, "APK verification passed for package: " + currentPackageName + ", v" + candidateVersionCode);
        return VerificationResult.success(candidateVersionCode);
    }

    public static long getInstalledVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return pInfo.getLongVersionCode();
            } else {
                return pInfo.versionCode;
            }
        } catch (Exception e) {
            UpdateLogger.e("Failed to get installed version code", e);
            return -1;
        }
    }

    public static long getArchiveVersionCode(PackageInfo archiveInfo) {
        if (archiveInfo == null) return -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return archiveInfo.getLongVersionCode();
        } else {
            return archiveInfo.versionCode;
        }
    }

    private static boolean verifySigningCertificates(Context context, PackageInfo archiveInfo) {
        try {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();

            Set<String> installedCertHashes = new HashSet<>();
            Set<String> archiveCertHashes = new HashSet<>();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo installedInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                if (installedInfo.signingInfo != null) {
                    Signature[] installedSigs = installedInfo.signingInfo.hasMultipleSigners()
                            ? installedInfo.signingInfo.getApkContentsSigners()
                            : installedInfo.signingInfo.getSigningCertificateHistory();
                    for (Signature sig : installedSigs) {
                        installedCertHashes.add(getSha256Digest(sig.toByteArray()));
                    }
                }

                if (archiveInfo.signingInfo != null) {
                    Signature[] archiveSigs = archiveInfo.signingInfo.hasMultipleSigners()
                            ? archiveInfo.signingInfo.getApkContentsSigners()
                            : archiveInfo.signingInfo.getSigningCertificateHistory();
                    for (Signature sig : archiveSigs) {
                        archiveCertHashes.add(getSha256Digest(sig.toByteArray()));
                    }
                }
            }

            // Fallback for older API or if signingInfo was null
            if (installedCertHashes.isEmpty() || archiveCertHashes.isEmpty()) {
                PackageInfo installedInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                if (installedInfo.signatures != null) {
                    for (Signature sig : installedInfo.signatures) {
                        installedCertHashes.add(getSha256Digest(sig.toByteArray()));
                    }
                }
                if (archiveInfo.signatures != null) {
                    for (Signature sig : archiveInfo.signatures) {
                        archiveCertHashes.add(getSha256Digest(sig.toByteArray()));
                    }
                }
            }

            if (installedCertHashes.isEmpty() || archiveCertHashes.isEmpty()) {
                UpdateLogger.w("Could not extract signatures from installed app or archive");
                return false;
            }

            // Verify that the candidate shares at least one common signing certificate
            for (String archiveHash : archiveCertHashes) {
                if (installedCertHashes.contains(archiveHash)) {
                    return true;
                }
            }

            UpdateLogger.w("Signatures do not match. Installed: " + installedCertHashes + ", Archive: " + archiveCertHashes);
            return false;

        } catch (Exception e) {
            UpdateLogger.e("Error verifying signing certificates", e);
            return false;
        }
    }

    public static String calculateFileSha256(File file) {
        if (file == null || !file.exists()) return null;
        try (InputStream is = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            UpdateLogger.e("Failed to calculate SHA-256 for " + file.getAbsolutePath(), e);
            return null;
        }
    }

    private static String getSha256Digest(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Arrays.toString(bytes);
        }
    }

    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            try {
                file.delete();
            } catch (Exception ignored) {}
        }
    }
}
