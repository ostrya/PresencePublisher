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
        // ensure we do not run publishing / scheduling in parallel
        synchronized (Scheduler.LOCK) {
            // we schedule the next runner first, in case acquiring foreground state fails and this
            // worker is terminated prematurely
            new Scheduler(getApplicationContext()).scheduleNext(id);
            try {
                setForegroundAsync(getForegroundInfo()).get();
            } catch (ExecutionException e) {
                DatabaseLogger.w(id, "Error while putting worker to foreground", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                DatabaseLogger.w(id, "Interrupted while putting worker to foreground");
            }

            try (NetworkBinder ignored = NetworkBinder.bindToNetwork(this)) {
                if (new Publisher(getApplicationContext()).publish()) {
                    DatabaseLogger.i(id, "Successfully published");
                }
            } catch (RuntimeException e) {
                DatabaseLogger.w(id, "Error while trying to publish", e);
            }
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
