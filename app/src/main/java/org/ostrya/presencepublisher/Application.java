package org.ostrya.presencepublisher;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.distance.ModelSpecificDistanceCalculator;
import org.altbeacon.beacon.logging.LogManager;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.ostrya.presencepublisher.beacon.BeaconManager;
import org.ostrya.presencepublisher.beacon.HyperlogLogger;
import org.ostrya.presencepublisher.log.CustomLogFormat;
import org.ostrya.presencepublisher.log.LogUncaughtExceptionHandler;
import org.ostrya.presencepublisher.receiver.SystemBroadcastReceiver;
import org.ostrya.presencepublisher.ui.notification.NotificationFactory;

import java.util.Collections;
import java.util.List;

import static org.ostrya.presencepublisher.ui.preference.condition.AddBeaconChoicePreferenceDummy.BEACON_LIST;

public class Application extends android.app.Application {
    public static final int PERMISSION_REQUEST_CODE = 1;
    public static final int LOCATION_REQUEST_CODE = 2;
    public static final int BATTERY_OPTIMIZATION_REQUEST_CODE = 3;
    public static final int ALARM_REQUEST_CODE = 4;
    public static final int MAIN_ACTIVITY_REQUEST_CODE = 5;
    public static final int START_BLUETOOTH_REQUEST_CODE = 6;
    public static final int ON_DEMAND_BLUETOOTH_REQUEST_CODE = 7;

    private BackgroundPowerSaver backgroundPowerSaver;

    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();
        initNetworkReceiver();
        initBeaconReceiver();
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
                new LogUncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()));
    }

    @SuppressWarnings("deprecation")
    private void initNetworkReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SystemBroadcastReceiver receiver = new SystemBroadcastReceiver();
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(receiver, filter);
        }
    }

    private void initBeaconReceiver() {
        if (supportsBeacons()) {
            LogManager.setLogger(new HyperlogLogger());
            Beacon.setHardwareEqualityEnforced(true);
            Beacon.setDistanceCalculator(new ModelSpecificDistanceCalculator(this, org.altbeacon.beacon.BeaconManager.getDistanceModelUpdateUrl()));
            org.altbeacon.beacon.BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
            List<BeaconParser> beaconParsers = beaconManager.getBeaconParsers();
            beaconParsers.add(new BeaconParser("iBeacon")
                    .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
            beaconParsers.add(new BeaconParser("Eddystone UID")
                    .setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (!sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet()).isEmpty()) {
                BeaconManager.getInstance().initialize(this);
            }
            if (Build.VERSION.SDK_INT >= 18) {
                backgroundPowerSaver = new BackgroundPowerSaver(this);
            }
        }
    }

    public boolean supportsBeacons() {
        return Build.VERSION.SDK_INT >= 18 && getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
}
