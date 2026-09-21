package com.vivek.updater;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class UpdateCheckWorker extends Worker {

    public static final String UNIQUE_PERIODIC_WORK_NAME = "VivekPanelPeriodicUpdateCheck";
    public static final String UNIQUE_ONE_TIME_WORK_NAME = "VivekPanelOneTimeUpdateCheck";

    public UpdateCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        UpdateLogger.i("UpdateCheckWorker started execution");
        boolean success = UpdateManager.getInstance(getApplicationContext()).checkForUpdateSync(true);
        if (success) {
            return Result.success();
        } else {
            // Retry with exponential backoff if temporary failure
            return Result.retry();
        }
    }

    public static void schedulePeriodicWork(Context context) {
        try {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                    UpdateCheckWorker.class, 6, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build();

            WorkManager.getInstance(context.getApplicationContext())
                    .enqueueUniquePeriodicWork(
                            UNIQUE_PERIODIC_WORK_NAME,
                            ExistingPeriodicWorkPolicy.KEEP,
                            request
                    );
            UpdateLogger.i("Enqueued periodic update check worker (6h interval)");
        } catch (Exception e) {
            UpdateLogger.e("Failed to schedule periodic update work", e);
        }
    }

    public static void enqueueImmediateWork(Context context) {
        try {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UpdateCheckWorker.class)
                    .setConstraints(constraints)
                    .build();

            WorkManager.getInstance(context.getApplicationContext())
                    .enqueueUniqueWork(
                            UNIQUE_ONE_TIME_WORK_NAME,
                            ExistingWorkPolicy.KEEP,
                            request
                    );
            UpdateLogger.i("Enqueued immediate update check worker");
        } catch (Exception e) {
            UpdateLogger.e("Failed to enqueue immediate update work", e);
        }
    }
}
