package org.ostrya.presencepublisher.schedule;

import static org.ostrya.presencepublisher.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.preference.condition.BeaconCategorySupport.BEACON_LIST;
import static org.ostrya.presencepublisher.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;
import static org.ostrya.presencepublisher.preference.schedule.ChargingMessageSchedulePreference.CHARGING_MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.preference.schedule.MessageSchedulePreference.MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.context.device.BatteryIntentLoader;
import org.ostrya.presencepublisher.notification.NotificationFactory;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Scheduler {
    private static final String TAG = "Scheduler";

    static final Object LOCK = new Object();

    private static final long NOW_DELAY = 1L;

    private static final String NEXT_WORKER_ID = "nextWorkerId";
    public static final String UNIQUE_WORKER_ID = "uniqueId";

    private static final String WORKER_1 = "PublishingWorker1";
    private static final String WORKER_2 = "PublishingWorker2";

    private final SharedPreferences sharedPreferences;
    private final WorkManager workManager;
    private final BatteryIntentLoader batteryIntentLoader;
    private final NotificationFactory notificationFactory;

    public Scheduler(Context context) {
        Context applicationContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        workManager = WorkManager.getInstance(applicationContext);
        batteryIntentLoader = new BatteryIntentLoader(applicationContext);
        notificationFactory = new NotificationFactory(applicationContext);
    }

    public void ensureSchedule() {
        synchronized (LOCK) {
            String nextWorker = getNextWorker();
            if (mustBeRescheduled(nextWorker)) {
                DatabaseLogger.i(TAG, "Starting schedule now.");
                scheduleWorker(NOW_DELAY, nextWorker);
            }
        }
    }

    public void runNow() {
        synchronized (LOCK) {
            DatabaseLogger.i(TAG, "Running now.");
            scheduleWorker(NOW_DELAY, getNextWorker());
        }
    }

    public void scheduleNext(String currentWorker) {
        synchronized (LOCK) {
            // we only switch workers when triggered from within a worker
            String nextWorker = WORKER_1.equals(currentWorker) ? WORKER_2 : WORKER_1;
            sharedPreferences.edit().putString(NEXT_WORKER_ID, nextWorker).apply();
            if (mustBeRescheduled(nextWorker)) {
                int minutes = 0;
                if (batteryIntentLoader.isCharging()) {
                    minutes = sharedPreferences.getInt(CHARGING_MESSAGE_SCHEDULE, 0);
                }
                if (minutes == 0) {
                    minutes = sharedPreferences.getInt(MESSAGE_SCHEDULE, 15);
                }
                long delay = minutes * 60L;
                long nextSchedule = System.currentTimeMillis() + delay * 1_000L;
                sharedPreferences.edit().putLong(NEXT_SCHEDULE, nextSchedule).apply();
                DatabaseLogger.i(
                        TAG,
                        "Scheduling next run at "
                                + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                        .format(new Date(nextSchedule)));
                notificationFactory.updateStatusNotification(
                        sharedPreferences.getLong(LAST_SUCCESS, 0L), nextSchedule);
                scheduleWorker(delay, nextWorker);
            }
        }
    }

    public void stopSchedule() {
        synchronized (LOCK) {
            workManager.cancelUniqueWork(WORKER_1);
            workManager.cancelUniqueWork(WORKER_2);
        }
    }

    @GuardedBy("LOCK")
    @NonNull
    private String getNextWorker() {
        return sharedPreferences.getString(NEXT_WORKER_ID, WORKER_1);
    }

    @GuardedBy("LOCK")
    private boolean mustBeRescheduled(String nextWorker) {
        try {
            return Futures.transform(
                            workManager.getWorkInfosForUniqueWork(nextWorker),
                            w -> w.isEmpty() || w.get(0).getState().isFinished(),
                            MoreExecutors.directExecutor())
                    .get(1, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            DatabaseLogger.w(TAG, "Unable to get worker state for " + nextWorker, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }

    @GuardedBy("LOCK")
    private void scheduleWorker(long delay, String workerId) {
        if (!sharedPreferences.getBoolean(LOCATION_CONSENT, false)) {
            DatabaseLogger.w(TAG, "Location consent not given, will not schedule anything.");
            return;
        }
        Constraints.Builder constraintsBuilder = new Constraints.Builder();
        if (useMobile()) {
            constraintsBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        } else {
            constraintsBuilder.setRequiredNetworkType(NetworkType.UNMETERED);
        }
        Constraints constraints = constraintsBuilder.build();
        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(PublishingWorker.class)
                        .setConstraints(constraints)
                        .keepResultsForAtLeast(1, TimeUnit.HOURS)
                        .setInitialDelay(delay, TimeUnit.SECONDS)
                        .setInputData(
                                new Data.Builder().putString(UNIQUE_WORKER_ID, workerId).build())
                        .build();
        DatabaseLogger.d(TAG, "Scheduling " + workerId + " in " + delay + " s");
        workManager.enqueueUniqueWork(workerId, ExistingWorkPolicy.REPLACE, workRequest);
    }

    private boolean useMobile() {
        return sharedPreferences.getBoolean(SEND_VIA_MOBILE_NETWORK, false)
                && (sharedPreferences.getBoolean(SEND_OFFLINE_MESSAGE, false)
                        || !sharedPreferences
                                .getStringSet(BEACON_LIST, Collections.emptySet())
                                .isEmpty());
    }
}
