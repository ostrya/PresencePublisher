package org.ostrya.presencepublisher;

import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_CONFIG_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_LIST;
import static org.ostrya.presencepublisher.ui.preference.schedule.ChargingMessageSchedulePreference.CHARGING_MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.MessageSchedulePreference.MESSAGE_SCHEDULE;

import static java.util.Collections.singletonList;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.distance.ModelSpecificDistanceCalculator;
import org.altbeacon.beacon.logging.LogManager;
import org.ostrya.presencepublisher.beacon.LoggerAdapter;
import org.ostrya.presencepublisher.beacon.PresenceBeaconManager;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.log.LogUncaughtExceptionHandler;
import org.ostrya.presencepublisher.message.MessageItem;
import org.ostrya.presencepublisher.ui.notification.NotificationFactory;
import org.ostrya.presencepublisher.ui.preference.messages.MessageConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PresencePublisher extends MultiDexApplication {
    private static final String TAG = "PresencePublisher";
    public static final int NOTIFICATION_ID = 1;
    public static final int NOTIFICATION_REQUEST_CODE = 2;

    // old preferences to be migrated
    private static final String PRESENCE_TOPIC = "topic";
    private static final String SEND_BATTERY_MESSAGE = "sendbatteryMessage";
    private static final String BATTERY_TOPIC = "batteryTopic";

    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();
        initBeaconManager();
        new NotificationFactory(this).createNotificationChannel();
        migrateOldSettings();
    }

    private void initLogger() {
        if (BuildConfig.DEBUG) {
            DatabaseLogger.initialize(this, Log.VERBOSE);
        } else {
            DatabaseLogger.initialize(this, Log.INFO);
        }
        Thread.setDefaultUncaughtExceptionHandler(
                new LogUncaughtExceptionHandler(this, Thread.getDefaultUncaughtExceptionHandler()));
    }

    private void initBeaconManager() {
        if (supportsBeacons()) {
            LogManager.setLogger(new LoggerAdapter());
            LogManager.setVerboseLoggingEnabled(BuildConfig.DEBUG);
            Beacon.setHardwareEqualityEnforced(true);
            Beacon.setDistanceCalculator(
                    new ModelSpecificDistanceCalculator(
                            this, BeaconManager.getDistanceModelUpdateUrl()));
            BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
            List<BeaconParser> beaconParsers = beaconManager.getBeaconParsers();
            beaconParsers.add(
                    new BeaconParser("iBeacon")
                            .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
            beaconParsers.add(
                    new BeaconParser("Eddystone UID")
                            .setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            Set<String> beacons =
                    sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet());
            if (!beacons.isEmpty()) {
                DatabaseLogger.i(TAG, "Enabling beacon scanning for " + beacons);
                PresenceBeaconManager.getInstance().initialize(this);
            } else {
                DatabaseLogger.i(
                        TAG, "No beacons configured, not enabling background beacon scanning");
            }
        }
    }

    private void migrateOldSettings() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preference.edit();
        String presenceTopic = preference.getString(PRESENCE_TOPIC, null);
        Set<String> messages =
                new HashSet<>(preference.getStringSet(MESSAGE_LIST, Collections.emptySet()));
        if (presenceTopic != null && !presenceTopic.isEmpty()) {
            String messageName = getString(R.string.default_presence_message_name);
            String value =
                    MessageConfiguration.toRawValue(
                            presenceTopic, singletonList(MessageItem.CONDITION_CONTENT));
            editor.putString(MESSAGE_CONFIG_PREFIX + messageName, value);
            messages.add(messageName);
        }
        String batteryTopic = preference.getString(BATTERY_TOPIC, null);
        if (batteryTopic != null
                && !batteryTopic.isEmpty()
                && preference.getBoolean(SEND_BATTERY_MESSAGE, false)) {
            String messageName = getString(R.string.default_battery_message_name);
            String value =
                    MessageConfiguration.toRawValue(
                            batteryTopic, singletonList(MessageItem.BATTERY_LEVEL));
            editor.putString(MESSAGE_CONFIG_PREFIX + messageName, value);
            messages.add(messageName);
        }
        int chargingMessageSchedule = preference.getInt(CHARGING_MESSAGE_SCHEDULE, 0);
        if (chargingMessageSchedule > 0 && chargingMessageSchedule < 5) {
            DatabaseLogger.w(TAG, "Migrating charging schedule to 5 minutes.");
            editor.putInt(CHARGING_MESSAGE_SCHEDULE, 5);
        }
        int messageSchedule = preference.getInt(MESSAGE_SCHEDULE, 15);
        if (messageSchedule < 5) {
            DatabaseLogger.w(TAG, "Migrating message schedule to 5 minutes.");
            editor.putInt(MESSAGE_SCHEDULE, 5);
        }
        editor.putStringSet(MESSAGE_LIST, messages)
                .remove(PRESENCE_TOPIC)
                .remove(PRESENCE_TOPIC)
                .remove(SEND_BATTERY_MESSAGE)
                .apply();
    }

    public boolean supportsBeacons() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
}
