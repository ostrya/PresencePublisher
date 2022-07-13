package org.ostrya.presencepublisher.schedule;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.Publisher;

public class PublishingWorker extends Worker {
    private static final String TAG = "PublishingWorker";

    public PublishingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (new Publisher(getApplicationContext()).publish()) {
                DatabaseLogger.i(TAG, "Successfully published.");
                return Result.success();
            }
        } catch (RuntimeException e) {
            DatabaseLogger.w(TAG, "Exception while trying to publish", e);
        }
        DatabaseLogger.i(TAG, "Problem while publishing, triggering retry.");
        return Result.retry();
    }
}
