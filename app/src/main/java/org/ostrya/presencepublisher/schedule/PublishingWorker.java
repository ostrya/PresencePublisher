package org.ostrya.presencepublisher.schedule;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;

import static org.ostrya.presencepublisher.PresencePublisher.LOCK;
import static org.ostrya.presencepublisher.PresencePublisher.PROGRESS_NOTIFICATION_ID;
import static org.ostrya.presencepublisher.schedule.Scheduler.UNIQUE_WORKER_ID;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.Publisher;
import org.ostrya.presencepublisher.ui.notification.NotificationFactory;

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
        DatabaseLogger.i(id, "Running publishing worker with attempt " + getRunAttemptCount());
        try {
            setForegroundAsync(getForegroundInfo()).get();
        } catch (ExecutionException e) {
            DatabaseLogger.w(id, "Error while putting worker to foreground", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            DatabaseLogger.w(id, "Interrupted while putting worker to foreground");
        }

        // make sure we do not run publishing in parallel
        synchronized (LOCK) {
            new Scheduler(getApplicationContext()).scheduleNext();

            try (NetworkBinder ignored = NetworkBinder.bindToNetwork(this)) {
                if (new Publisher(getApplicationContext()).publish()) {
                    DatabaseLogger.i(id, "Successfully published.");
                }
            } catch (RuntimeException e) {
                DatabaseLogger.w(id, "Error while trying to publish", e);
            }
            return Result.success();
        }
    }

    @NonNull
    private ForegroundInfo getForegroundInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new ForegroundInfo(
                    PROGRESS_NOTIFICATION_ID,
                    notificationFactory.getProgressNotification(),
                    FOREGROUND_SERVICE_TYPE_DATA_SYNC | FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            return new ForegroundInfo(
                    PROGRESS_NOTIFICATION_ID, notificationFactory.getProgressNotification());
        }
    }
}
