package org.ostrya.presencepublisher.schedule;

import static org.ostrya.presencepublisher.PresencePublisher.NOTIFICATION_ID;
import static org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;
import static org.ostrya.presencepublisher.ui.preference.schedule.ChargingMessageSchedulePreference.CHARGING_MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.MessageSchedulePreference.MESSAGE_SCHEDULE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private static final String TAG = "Scheduler";

    private static final String ID = "PublisherWork";
    private static final String ID2 = "PublisherWork2";
    private static final String ID3 = "PublisherWork3";
    private static final String CID = "ChargingPublisherWork";
    private static final String CID2 = "ChargingPublisherWork2";
    private static final String CID3 = "ChargingPublisherWork3";

    public static final long NOW_DELAY = 1_000L;

    private final Context applicationContext;
    private final SharedPreferences sharedPreferences;
    private final WorkManager workManager;

    public Scheduler(Context context) {
        applicationContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        workManager = WorkManager.getInstance(applicationContext);
    }

    public void startSchedule() {
        if (!sharedPreferences.getBoolean(LOCATION_CONSENT, false)) {
            DatabaseLogger.w(TAG, "Location consent not given, will not schedule anything.");
            return;
        }
        int messageScheduleInMinutes = sharedPreferences.getInt(MESSAGE_SCHEDULE, 15);
        boolean useMobile = useMobile();
        scheduleWorker(messageScheduleInMinutes, useMobile, false);
        int chargingScheduleInMinutes = sharedPreferences.getInt(CHARGING_MESSAGE_SCHEDULE, 0);
        if (chargingScheduleInMinutes > 0) {
            scheduleWorker(chargingScheduleInMinutes, useMobile, true);
        }
    }

    private void scheduleWorker(int scheduleInMinutes, boolean useMobile, boolean chargingOnly) {
        Constraints.Builder constraintsBuilder = new Constraints.Builder();
        if (chargingOnly) {
            constraintsBuilder.setRequiresCharging(true);
        }
        if (useMobile) {
            constraintsBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        } else {
            constraintsBuilder.setRequiredNetworkType(NetworkType.UNMETERED);
        }
        Constraints constraints = constraintsBuilder.build();
        if (scheduleInMinutes >= 15) {
            workManager.enqueueUniquePeriodicWork(
                    chargingOnly ? CID : ID,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    new PeriodicWorkRequest.Builder(
                                    PublishingWorker.class, scheduleInMinutes, TimeUnit.MINUTES)
                            .setConstraints(constraints)
                            .build());
        } else if (scheduleInMinutes >= 8) {
            workManager.enqueueUniquePeriodicWork(
                    chargingOnly ? CID : ID,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    new PeriodicWorkRequest.Builder(
                                    PublishingWorker.class,
                                    scheduleInMinutes * 2L,
                                    TimeUnit.MINUTES)
                            .setConstraints(constraints)
                            .build());
            workManager.enqueueUniquePeriodicWork(
                    chargingOnly ? CID2 : ID2,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    new PeriodicWorkRequest.Builder(
                                    PublishingWorker.class,
                                    scheduleInMinutes * 2L,
                                    TimeUnit.MINUTES)
                            .setInitialDelay(scheduleInMinutes, TimeUnit.MINUTES)
                            .setConstraints(constraints)
                            .build());
        } else {
            workManager.enqueueUniquePeriodicWork(
                    chargingOnly ? CID : ID,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    new PeriodicWorkRequest.Builder(
                                    PublishingWorker.class,
                                    scheduleInMinutes * 3L,
                                    TimeUnit.MINUTES)
                            .setConstraints(constraints)
                            .build());
            workManager.enqueueUniquePeriodicWork(
                    chargingOnly ? CID2 : ID2,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    new PeriodicWorkRequest.Builder(
                                    PublishingWorker.class,
                                    scheduleInMinutes * 3L,
                                    TimeUnit.MINUTES)
                            .setInitialDelay(scheduleInMinutes, TimeUnit.MINUTES)
                            .setConstraints(constraints)
                            .build());
            workManager.enqueueUniquePeriodicWork(
                    chargingOnly ? CID3 : ID3,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    new PeriodicWorkRequest.Builder(
                                    PublishingWorker.class,
                                    scheduleInMinutes * 3L,
                                    TimeUnit.MINUTES)
                            .setInitialDelay(scheduleInMinutes * 2L, TimeUnit.MINUTES)
                            .setConstraints(constraints)
                            .build());
        }
        NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_ID);
    }

    public void stopSchedule() {
        workManager.cancelUniqueWork(ID);
        workManager.cancelUniqueWork(ID2);
        workManager.cancelUniqueWork(ID3);
        workManager.cancelUniqueWork(CID);
        workManager.cancelUniqueWork(CID2);
        workManager.cancelUniqueWork(CID3);
    }

    private boolean useMobile() {
        return sharedPreferences.getBoolean(SEND_VIA_MOBILE_NETWORK, false)
                && (sharedPreferences.getBoolean(SEND_OFFLINE_MESSAGE, false)
                        || !sharedPreferences
                                .getStringSet(BEACON_LIST, Collections.emptySet())
                                .isEmpty());
    }
}
