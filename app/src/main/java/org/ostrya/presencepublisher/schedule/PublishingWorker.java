package org.ostrya.presencepublisher.schedule;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;

import static org.ostrya.presencepublisher.PresencePublisher.PROGRESS_NOTIFICATION_ID;
import static org.ostrya.presencepublisher.log.FormatArgs.args;
import static org.ostrya.presencepublisher.schedule.Scheduler.UNIQUE_WORKER_ID;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.Publisher;
import org.ostrya.presencepublisher.notification.NotificationFactory;

import java.util.concurrent.ExecutionException;

public class PublishingWorker extends Worker {
    private final NotificationFactory notificationFactory;

    public PublishingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationFactory = new NotificationFactory(getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        String id = getInputData().getString(UNIQUE_WORKER_ID);
        DatabaseLogger.i(
                id, "Running publishing worker with attempt %d", args(getRunAttemptCount()));
        try {
            // requesting foreground before the lock to improve chances this thread is not killed
            // while waiting for the lock
            setForegroundAsync(getForegroundInfo()).get();
        } catch (ExecutionException e) {
            // funnily enough, if the two workers end up running in parallel, we may get an
            // IllegalStateException in the later worker if the earlier worker returns at the same
            // time
            // but it looks like we're still benefitting from the other worker having been in the
            // foreground, as the worker still finishes successfully
            DatabaseLogger.w(id, "Error while putting worker to foreground", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            DatabaseLogger.w(id, "Interrupted while putting worker to foreground");
        }

        // make sure we do not run publishing / scheduling in parallel
        synchronized (Scheduler.LOCK) {
            try (NetworkBinder ignored = NetworkBinder.bindToNetwork(this)) {
                if (new Publisher(getApplicationContext()).publish()) {
                    DatabaseLogger.i(id, "Successfully published");
                }
            } catch (RuntimeException e) {
                DatabaseLogger.w(id, "Error while trying to publish", e);
            }
            new Scheduler(getApplicationContext()).scheduleNext(id);
            return Result.success();
        }
    }

    @NonNull
    @Override
    public ForegroundInfo getForegroundInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new ForegroundInfo(
                    PROGRESS_NOTIFICATION_ID,
                    notificationFactory.getProgressNotification(),
                    FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            return new ForegroundInfo(
                    PROGRESS_NOTIFICATION_ID, notificationFactory.getProgressNotification());
        }
    }
}
