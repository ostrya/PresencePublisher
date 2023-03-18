package org.ostrya.presencepublisher.schedule;

import static org.ostrya.presencepublisher.PresencePublisher.LOCK;
import static org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;
import static org.ostrya.presencepublisher.ui.preference.schedule.ChargingMessageSchedulePreference.CHARGING_MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.schedule.MessageSchedulePreference.MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.notification.NotificationFactory;
import org.ostrya.presencepublisher.util.BatteryIntentLoader;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private static final String TAG = "Scheduler";

    public static final long NOW_DELAY = 1_000L;

    public static final String USE_WORKER_1 = "useWorker1";
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
        long scheduled = sharedPreferences.getLong(NEXT_SCHEDULE, 0L);
        long now = System.currentTimeMillis();
        long delay = scheduled - now;
        if (delay <= NOW_DELAY) {
            DatabaseLogger.i(TAG, "Starting schedule now.");
            scheduleWorker(NOW_DELAY);
        }
    }

    public void runNow() {
        DatabaseLogger.i(TAG, "Running now.");
        scheduleWorker(NOW_DELAY);
    }

    public void scheduleNext() {
        // avoid race condition when scheduling next run
        synchronized (LOCK) {
            long scheduled = sharedPreferences.getLong(NEXT_SCHEDULE, 0L);
            long now = System.currentTimeMillis();
            long delay = scheduled - now;
            if (delay <= NOW_DELAY) {
                int minutes = 0;
                if (batteryIntentLoader.isCharging()) {
                    minutes = sharedPreferences.getInt(CHARGING_MESSAGE_SCHEDULE, 0);
                }
                if (minutes == 0) {
                    minutes = sharedPreferences.getInt(MESSAGE_SCHEDULE, 15);
                }
                delay = minutes * 60_000L;
                long nextSchedule = now + delay;
                sharedPreferences.edit().putLong(NEXT_SCHEDULE, nextSchedule).apply();
                DatabaseLogger.i(
                        TAG,
                        "Scheduling next run at "
                                + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                        .format(new Date(nextSchedule)));
                notificationFactory.updateStatusNotification(
                        sharedPreferences.getLong(LAST_SUCCESS, 0L), nextSchedule);
                scheduleWorker(delay);
            }
        }
    }

    private void scheduleWorker(long delay) {
        // avoid race condition when selecting worker
        synchronized (LOCK) {
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
            boolean useWorker1 = sharedPreferences.getBoolean(USE_WORKER_1, false);
            String uniqueWorkName = useWorker1 ? WORKER_1 : WORKER_2;
            OneTimeWorkRequest workRequest =
                    new OneTimeWorkRequest.Builder(PublishingWorker.class)
                            .setConstraints(constraints)
                            .keepResultsForAtLeast(1, TimeUnit.HOURS)
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(
                                    new Data.Builder()
                                            .putString(UNIQUE_WORKER_ID, uniqueWorkName)
                                            .build())
                            .build();
            workManager.enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, workRequest);
            sharedPreferences.edit().putBoolean(USE_WORKER_1, !useWorker1).apply();
        }
    }

    public void stopSchedule() {
        workManager.cancelUniqueWork(WORKER_1);
        workManager.cancelUniqueWork(WORKER_2);
    }

    private boolean useMobile() {
        return sharedPreferences.getBoolean(SEND_VIA_MOBILE_NETWORK, false)
                && (sharedPreferences.getBoolean(SEND_OFFLINE_MESSAGE, false)
                        || !sharedPreferences
                                .getStringSet(BEACON_LIST, Collections.emptySet())
                                .isEmpty());
    }
}
