package org.ostrya.presencepublisher;

import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_LIST;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

import com.google.android.material.color.DynamicColors;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.distance.ModelSpecificDistanceCalculator;
import org.altbeacon.beacon.logging.LogManager;
import org.ostrya.presencepublisher.beacon.LoggerAdapter;
import org.ostrya.presencepublisher.beacon.PresenceBeaconManager;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.log.LogUncaughtExceptionHandler;
import org.ostrya.presencepublisher.notification.NotificationFactory;
import org.ostrya.presencepublisher.ui.preference.about.NightModePreference;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PresencePublisher extends MultiDexApplication {
    private static final String TAG = "PresencePublisher";
    public static final int STATUS_NOTIFICATION_ID = 1;
    public static final int NOTIFICATION_REQUEST_CODE = 2;
    public static final int PROGRESS_NOTIFICATION_ID = 3;

    public static final Object LOCK = new Object();

    public static final String MQTT_CLIENT_ID = "mqttClientId";

    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();
        initBeaconManager();
        new NotificationFactory(this).createNotificationChannels();
        initClientId();
        NightModePreference.updateCurrentNightMode(this);
        DynamicColors.applyToActivitiesIfAvailable(this);
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

    private void initClientId() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preference.contains(MQTT_CLIENT_ID)) {
            DatabaseLogger.i(TAG, "Generating persistent client ID");
            preference.edit().putString(MQTT_CLIENT_ID, UUID.randomUUID().toString()).apply();
        }
    }

    public boolean supportsBeacons() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
}
