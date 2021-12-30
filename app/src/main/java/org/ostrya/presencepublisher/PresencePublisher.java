package org.ostrya.presencepublisher;

import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_LIST;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.hypertrack.hyperlog.HyperLog;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.distance.ModelSpecificDistanceCalculator;
import org.altbeacon.beacon.logging.LogManager;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.ostrya.presencepublisher.beacon.HyperlogLogger;
import org.ostrya.presencepublisher.beacon.PresenceBeaconManager;
import org.ostrya.presencepublisher.log.CustomLogFormat;
import org.ostrya.presencepublisher.log.LogUncaughtExceptionHandler;
import org.ostrya.presencepublisher.ui.notification.NotificationFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PresencePublisher extends android.app.Application {
    private static final String TAG = "PresencePublisher";
    public static final int ALARM_PENDING_INTENT_REQUEST_CODE = 1;
    public static final int NOTIFICATION_REQUEST_CODE = 2;
    public static final int NETWORK_PENDING_INTENT_REQUEST_CODE = 3;
    public static final String ALARM_ACTION = "org.ostrya.presencepublisher.ALARM";
    public static final String NETWORK_PENDING_INTENT_ACTION =
            "org.ostrya.presencepublisher.NETWORK_PENDING_INTENT";

    @SuppressWarnings("FieldCanBeLocal")
    private BackgroundPowerSaver backgroundPowerSaver;

    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();
        initBeaconManager();
        NotificationFactory.createNotificationChannel(this);
    }

    private void initLogger() {
        HyperLog.initialize(this, new CustomLogFormat(this));
        if (BuildConfig.DEBUG) {
            HyperLog.setLogLevel(Log.VERBOSE);
        } else {
            HyperLog.setLogLevel(Log.INFO);
        }
        Thread.setDefaultUncaughtExceptionHandler(
                new LogUncaughtExceptionHandler(this, Thread.getDefaultUncaughtExceptionHandler()));
    }

    private void initBeaconManager() {
        if (supportsBeacons()) {
            LogManager.setLogger(new HyperlogLogger());
            LogManager.setVerboseLoggingEnabled(BuildConfig.DEBUG);
            Beacon.setHardwareEqualityEnforced(true);
            Beacon.setDistanceCalculator(
                    new ModelSpecificDistanceCalculator(
                            this, BeaconManager.getDistanceModelUpdateUrl()));
            BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
            beaconManager.setBackgroundMode(true);
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
                HyperLog.i(TAG, "Enabling beacon scanning for " + beacons);
                PresenceBeaconManager.getInstance().initialize(this);
            } else {
                HyperLog.i(TAG, "No beacons configured, not enabling background beacon scanning");
                return;
            }
            if (Build.VERSION.SDK_INT >= 18) {
                backgroundPowerSaver = new BackgroundPowerSaver(this);
            }
        }
    }

    public boolean supportsBeacons() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
}
