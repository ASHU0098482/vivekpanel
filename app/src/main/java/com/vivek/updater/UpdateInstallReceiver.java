package com.vivek.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;

public class UpdateInstallReceiver extends BroadcastReceiver {

    public static final String ACTION_INSTALL_STATUS = "com.vivek.action.INSTALL_STATUS";
    public static final int STATUS_FAILURE_TIMEOUT = 8;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
        int sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1);
        String packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME);

        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                UpdateLogger.log(UpdateLogger.INSTALL_PENDING_USER_ACTION,
                        "Pending user confirmation for session " + sessionId + ": " + message);
                Intent confirmIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                if (confirmIntent != null) {
                    confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(confirmIntent);
                    } catch (Exception e) {
                        UpdateLogger.e("Failed to launch PackageInstaller confirmation intent", e);
                    }
                }
                break;

            case PackageInstaller.STATUS_SUCCESS:
                UpdateLogger.log(UpdateLogger.INSTALL_SUCCESS,
                        "Package update installed successfully for " + packageName + " (session " + sessionId + ")");
                UpdateManager.getInstance(context).onInstallSuccess();
                break;

            case PackageInstaller.STATUS_FAILURE:
            case PackageInstaller.STATUS_FAILURE_ABORTED:
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
            case PackageInstaller.STATUS_FAILURE_INVALID:
            case PackageInstaller.STATUS_FAILURE_STORAGE:
            case STATUS_FAILURE_TIMEOUT:
            default:
                String statusName = getStatusString(status);
                UpdateLogger.log(UpdateLogger.INSTALL_FAILED,
                        "Install failed [" + statusName + " (" + status + ")]: " + message + " (session " + sessionId + ")");
                UpdateManager.getInstance(context).onInstallFailed(status, message);
                break;
        }
    }

    private static String getStatusString(int status) {
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION: return "STATUS_PENDING_USER_ACTION";
            case PackageInstaller.STATUS_SUCCESS: return "STATUS_SUCCESS";
            case PackageInstaller.STATUS_FAILURE: return "STATUS_FAILURE";
            case PackageInstaller.STATUS_FAILURE_ABORTED: return "STATUS_FAILURE_ABORTED";
            case PackageInstaller.STATUS_FAILURE_BLOCKED: return "STATUS_FAILURE_BLOCKED";
            case PackageInstaller.STATUS_FAILURE_CONFLICT: return "STATUS_FAILURE_CONFLICT";
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE: return "STATUS_FAILURE_INCOMPATIBLE";
            case PackageInstaller.STATUS_FAILURE_INVALID: return "STATUS_FAILURE_INVALID";
            case PackageInstaller.STATUS_FAILURE_STORAGE: return "STATUS_FAILURE_STORAGE";
            case STATUS_FAILURE_TIMEOUT: return "STATUS_FAILURE_TIMEOUT";
            default: return "STATUS_UNKNOWN";
        }
    }
}
