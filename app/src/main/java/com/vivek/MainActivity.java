package com.vivek;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;


public class MainActivity extends Activity {

    public static MainActivity instance;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    private static final int INSTALL_UNKNOWN_APPS_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        // Schedule periodic background update check via WorkManager
        com.vivek.updater.UpdateCheckWorker.schedulePeriodicWork(this);

        // Prompt for unknown apps permission once if needed, then trigger immediate background update check
        com.vivek.updater.UpdateManager.getInstance(this).promptInstallUnknownAppsOnce(this, INSTALL_UNKNOWN_APPS_REQUEST_CODE);
        com.vivek.updater.UpdateManager.getInstance(this).checkForUpdate(true);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        startAppFlow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fully terminate process so floating window and drawing loops stop running in background
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void startAppFlow() {
        RemoteConfig.fetchConfig(() -> {
            runOnUiThread(() -> {
                if (!RemoteConfig.isOnline) {
                    showMaintenanceDialog(RemoteConfig.maintenanceMessage);
                    return;
                }

                int localVersion = 1;
                try {
                    localVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (RemoteConfig.remoteVersionCode > localVersion) {
                    showUpdateDialog(RemoteConfig.updateUrl);
                    return;
                }

                // If already updated, suppress any stale update notices; only show announcements once
                android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                String lastSeenNotice = prefs.getString("last_seen_notice", "");
                String currentNoticeKey = (RemoteConfig.noticeTitle != null ? RemoteConfig.noticeTitle : "") + "_" + 
                                          (RemoteConfig.noticeMessage != null ? RemoteConfig.noticeMessage : "");

                if (RemoteConfig.showNotice 
                        && RemoteConfig.noticeMessage != null 
                        && !RemoteConfig.noticeMessage.isEmpty()
                        && !currentNoticeKey.equals(lastSeenNotice)) {

                    boolean isUpdateNotice = (RemoteConfig.noticeTitle != null && RemoteConfig.noticeTitle.toLowerCase().contains("update"))
                            || (RemoteConfig.noticeMessage.toLowerCase().contains("update is available"));

                    if (!isUpdateNotice) {
                        showNoticeDialog(RemoteConfig.noticeTitle, RemoteConfig.noticeMessage, () -> {
                            prefs.edit().putString("last_seen_notice", currentNoticeKey).apply();
                            showFirstSplash();
                        });
                        return;
                    }
                }

                showFirstSplash();
            });
        });
    }

    public void showUpdateDialog(final String updateUrl) {
        String validUpdateUrl = (updateUrl != null && !updateUrl.isEmpty())
                ? updateUrl
                : "https://raw.githubusercontent.com/ASHU0098482/vivekpanel/main/VIVEK_PANEL.apk";
        String notes = (RemoteConfig.releaseNotes != null && !RemoteConfig.releaseNotes.isEmpty())
                ? "\n\nWhat's new:\n" + RemoteConfig.releaseNotes
                : "";
        String msg = "A new update (v" + RemoteConfig.versionName + ") is available!" + notes + "\n\nTap 'UPDATE NOW' to install the latest version.";
        String title = "🔄 UPDATE AVAILABLE";

        new android.app.AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("UPDATE NOW", (d, which) -> {
                    d.dismiss();
                    android.app.ProgressDialog progress = new android.app.ProgressDialog(MainActivity.this, android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                    progress.setTitle("Downloading Update");
                    progress.setMessage("Downloading update... Please wait.");
                    progress.setCancelable(false);
                    progress.show();

                    new Thread(() -> {
                        boolean success = com.vivek.updater.UpdateManager.getInstance(MainActivity.this).checkForUpdateSync(false);
                        runOnUiThread(() -> {
                            try {
                                if (progress.isShowing()) {
                                    progress.dismiss();
                                }
                            } catch (Exception ignored) {}
                            if (!success) {
                                Toast.makeText(MainActivity.this, "Update download failed or cooled off. Please retry.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton(RemoteConfig.forceUpdate ? "EXIT" : "LATER", (d, which) -> {
                    d.dismiss();
                    if (RemoteConfig.forceUpdate) {
                        finishAffinity();
                    } else {
                        showFirstSplash();
                    }
                })
                .create()
                .show();
    }

    private void showNoticeDialog(String title, String message, Runnable onContinue) {
        String displayTitle = (title != null && !title.isEmpty()) ? title : "📢 Notice";
        new android.app.AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(displayTitle)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK", (d, which) -> {
                d.dismiss();
                if (onContinue != null) {
                    onContinue.run();
                }
            })
            .create()
            .show();
    }

    private void showMaintenanceDialog(String message) {
        new android.app.AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("Under Maintenance")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("EXIT", (d, which) -> {
                finishAffinity();
            })
            .create()
            .show();
    }

    public void checkForUpdates(final boolean showToastIfUpToDate) {
        com.vivek.updater.UpdateManager.getInstance(this).checkForUpdate(!showToastIfUpToDate);
    }

    public void downloadAndInstallApk(final String apkUrl) {
        com.vivek.updater.UpdateManager.getInstance(this).checkForUpdate(false);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showFirstSplash() {
        // === ANIMATED SPLASH INTRO (inspired by example video) ===
        // Black fullscreen background
        final android.widget.FrameLayout splashRoot = new android.widget.FrameLayout(this);
        splashRoot.setBackgroundColor(Color.BLACK);
        splashRoot.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));

        // --- PHASE 1: Logo in center, starts small and zooms in ---
        final ImageView logoView = new ImageView(this);
        android.widget.FrameLayout.LayoutParams logoParams = new android.widget.FrameLayout.LayoutParams(
                dpToPx(180), dpToPx(180));
        logoParams.gravity = android.view.Gravity.CENTER;
        logoView.setLayoutParams(logoParams);
        logoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logoView.setAlpha(0f);
        logoView.setScaleX(0.15f);
        logoView.setScaleY(0.15f);
        int iconResId = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName());
        if (iconResId != 0) {
            logoView.setImageResource(iconResId);
        }
        splashRoot.addView(logoView);

        // Load logo from remote config or fallback
        String logoUrl = null;
        if (com.vivek.RemoteConfig.logoUrl != null && !com.vivek.RemoteConfig.logoUrl.isEmpty()) {
            logoUrl = com.vivek.RemoteConfig.logoUrl;
            if (logoUrl.contains("?")) {
                logoUrl += "&t=" + System.currentTimeMillis();
            } else {
                logoUrl += "?t=" + System.currentTimeMillis();
            }
        }
        if (logoUrl != null) {
            com.bumptech.glide.Glide.with(this)
                .asBitmap()
                .load(logoUrl)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                    @Override
                    public void onResourceReady(@androidx.annotation.NonNull android.graphics.Bitmap resource,
                            @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                        android.graphics.Bitmap transparentBitmap = com.vivek.Utils.makeBlackTransparent(resource);
                        logoView.setImageBitmap(transparentBitmap);
                    }
                    @Override
                    public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {}
                });
        }

        // --- PHASE 2: App name text (letter-by-letter) ---
        final TextView splashText = new TextView(this);
        // Get app name from remote config
        String appName = (com.vivek.RemoteConfig.appName != null && !com.vivek.RemoteConfig.appName.isEmpty())
                ? com.vivek.RemoteConfig.appName : "VIVEK PANEL";
        splashText.setText("");
        splashText.setTextSize(36);
        splashText.setTextColor(Color.parseColor("#00D2FF")); // Golden accent
        splashText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        splashText.setGravity(android.view.Gravity.CENTER);
        splashText.setAlpha(0f);
        // Neon glow shadow
        splashText.setShadowLayer(30, 0, 0, Color.parseColor("#00D2FF"));
        android.widget.FrameLayout.LayoutParams textParams = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
        textParams.gravity = android.view.Gravity.CENTER;
        textParams.topMargin = dpToPx(130);
        splashText.setLayoutParams(textParams);
        splashText.setLetterSpacing(0.15f);
        splashRoot.addView(splashText);

        // --- PHASE 3: Subtitle glow line ---
        final View glowLine = new View(this);
        android.widget.FrameLayout.LayoutParams glowParams = new android.widget.FrameLayout.LayoutParams(
                0, dpToPx(2));
        glowParams.gravity = android.view.Gravity.CENTER_HORIZONTAL | android.view.Gravity.CENTER_VERTICAL;
        glowParams.topMargin = dpToPx(185);
        glowLine.setLayoutParams(glowParams);
        glowLine.setBackgroundColor(Color.parseColor("#00D2FF"));
        glowLine.setAlpha(0f);
        splashRoot.addView(glowLine);

        setContentView(splashRoot);

        final Handler handler = new Handler();
        final String finalAppName = appName;

        // ====== ANIMATION SEQUENCE ======

        // STEP 1: Logo zoom-in + fade-in (0ms - 800ms)
        handler.postDelayed(() -> {
            logoView.animate()
                .alpha(1f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(800)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.5f))
                .start();
        }, 200);

        // STEP 2: Logo pulse glow effect (800ms - 1400ms)
        handler.postDelayed(() -> {
            logoView.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(300)
                .withEndAction(() -> {
                    logoView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(300)
                        .start();
                })
                .start();
        }, 1100);

        // STEP 3: Logo shrinks up + text starts appearing letter by letter (1500ms+)
        handler.postDelayed(() -> {
            // Move logo up
            logoView.animate()
                .translationY(-dpToPx(60))
                .scaleX(0.7f)
                .scaleY(0.7f)
                .setDuration(500)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

            // Start letter-by-letter text reveal
            splashText.setAlpha(1f);
            final int[] charIndex = {0};
            final int letterDelay = 80; // ms per letter
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (charIndex[0] <= finalAppName.length()) {
                        splashText.setText(finalAppName.substring(0, charIndex[0]));
                        // Pulse the glow intensity
                        float glowRadius = 20 + (charIndex[0] % 3) * 10;
                        splashText.setShadowLayer(glowRadius, 0, 0, Color.parseColor("#00D2FF"));
                        charIndex[0]++;
                        handler.postDelayed(this, letterDelay);
                    }
                }
            }, 300);
        }, 1600);

        // STEP 4: Glow line expands (after text is fully revealed)
        int textRevealDuration = 1600 + 300 + (appName.length() * 80) + 200;
        handler.postDelayed(() -> {
            glowLine.setAlpha(1f);
            android.animation.ValueAnimator lineAnim = android.animation.ValueAnimator.ofInt(0, dpToPx(200));
            lineAnim.setDuration(400);
            lineAnim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
            lineAnim.addUpdateListener(animation -> {
                int val = (int) animation.getAnimatedValue();
                android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) glowLine.getLayoutParams();
                lp.width = val;
                glowLine.setLayoutParams(lp);
            });
            lineAnim.start();
        }, textRevealDuration);

        // STEP 5: Full neon glow pulse on text (peak moment)
        handler.postDelayed(() -> {
            // Intense glow pulse
            android.animation.ValueAnimator glowAnim = android.animation.ValueAnimator.ofFloat(30f, 60f, 30f);
            glowAnim.setDuration(600);
            glowAnim.setRepeatCount(1);
            glowAnim.addUpdateListener(animation -> {
                float radius = (float) animation.getAnimatedValue();
                splashText.setShadowLayer(radius, 0, 0, Color.parseColor("#00D2FF"));
            });
            glowAnim.start();
        }, textRevealDuration + 200);

        // STEP 6: Fade out everything and proceed to login (after all animations)
        int totalSplashDuration = textRevealDuration + 1200;
        handler.postDelayed(() -> {
            // Fade out all splash elements
            splashRoot.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction(() -> {
                    // Proceed to overlay permission check -> Login
                    checkOverlayPermission();
                })
                .start();
        }, totalSplashDuration);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }


    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission is required!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                return;
            }
        }
        startLogin();
    }

    private void startLogin() {
        new Login(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission denied! Exiting...", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                startLogin();
            }
        } else if (requestCode == INSTALL_UNKNOWN_APPS_REQUEST_CODE) {
            if (com.vivek.updater.UpdateManager.getInstance(this).canRequestPackageInstalls()) {
                com.vivek.updater.UpdateManager.getInstance(this).checkForUpdate(true);
            }
        }
    }
}
