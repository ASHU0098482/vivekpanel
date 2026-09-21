package com.vivek.updater;

import android.util.Log;

public final class UpdateLogger {

    public static final String TAG = "AutoUpdater";

    public static final String AUTO_UPDATE_CHECK = "AUTO_UPDATE_CHECK";
    public static final String UPDATE_AVAILABLE = "UPDATE_AVAILABLE";
    public static final String DOWNLOAD_STARTED = "DOWNLOAD_STARTED";
    public static final String DOWNLOAD_COMPLETE = "DOWNLOAD_COMPLETE";
    public static final String APK_VERIFIED = "APK_VERIFIED";
    public static final String INSTALL_SESSION_CREATED = "INSTALL_SESSION_CREATED";
    public static final String SILENT_INSTALL_REQUESTED = "SILENT_INSTALL_REQUESTED";
    public static final String INSTALL_PENDING_USER_ACTION = "INSTALL_PENDING_USER_ACTION";
    public static final String INSTALL_SUCCESS = "INSTALL_SUCCESS";
    public static final String INSTALL_FAILED = "INSTALL_FAILED";

    private static boolean debugMode = true;

    private UpdateLogger() {}

    public static void setDebug(boolean enabled) {
        debugMode = enabled;
    }

    public static void log(String event, String message) {
        if (debugMode) {
            Log.d(TAG, "[" + event + "] " + (message != null ? message : ""));
        }
    }

    public static void i(String message) {
        if (debugMode) {
            Log.i(TAG, message);
        }
    }

    public static void w(String message) {
        Log.w(TAG, message);
    }

    public static void e(String message, Throwable tr) {
        Log.e(TAG, message, tr);
    }
}
