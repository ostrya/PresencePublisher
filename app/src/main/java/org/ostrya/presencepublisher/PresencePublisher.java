package org.ostrya.presencepublisher;

import static org.ostrya.presencepublisher.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.preference.condition.BeaconCategorySupport.BEACON_LIST;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

import com.google.android.material.color.DynamicColors;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Settings;
import org.altbeacon.beacon.logging.LogManager;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;
import org.ostrya.presencepublisher.log.BeaconLoggerAdapter;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.log.LogUncaughtExceptionHandler;
import org.ostrya.presencepublisher.log.PahoNoopLogger;
import org.ostrya.presencepublisher.mqtt.context.condition.beacon.PresenceBeaconManager;
import org.ostrya.presencepublisher.mqtt.context.condition.network.NetworkService;
import org.ostrya.presencepublisher.notification.NotificationFactory;
import org.ostrya.presencepublisher.preference.about.NightModePreference;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PresencePublisher extends MultiDexApplication {
    private static final String TAG = "PresencePublisher";
    public static final int STATUS_NOTIFICATION_ID = 1;
    public static final int NOTIFICATION_REQUEST_CODE = 2;
    public static final int PROGRESS_NOTIFICATION_ID = 3;

    public static final String MQTT_CLIENT_ID = "mqttClientId";

    private static final String USE_WORKER_1 = "useWorker1";
    private static final String CURRENT_WIFI_SSID = "currentWifiSsid";

    private final AtomicReference<ConnectivityManager.NetworkCallback> currentCallback =
            new AtomicReference<>();

    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();
        initBeaconManager();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean(LOCATION_CONSENT, false)) {
            setUpConditionCallbacks(preferences);
        }
        new NotificationFactory(this).createNotificationChannels();
        initClientId(preferences);
        NightModePreference.updateCurrentNightMode(preferences);
        DynamicColors.applyToActivitiesIfAvailable(this);
        removeOldValues(preferences);
    }

    public void setUpConditionCallbacks(SharedPreferences preferences) {
        initNetworkCallback();
        initBeaconCallback(preferences);
    }

    private void initLogger() {
        if (BuildConfig.DEBUG) {
            DatabaseLogger.initialize(this, Log.VERBOSE);
        } else {
            DatabaseLogger.initialize(this, Log.INFO);
        }
        Thread.setDefaultUncaughtExceptionHandler(
                new LogUncaughtExceptionHandler(this, Thread.getDefaultUncaughtExceptionHandler()));
        LoggerFactory.setLogger(PahoNoopLogger.class.getName());
    }

    private void initBeaconManager() {
        if (supportsBeacons()) {
            LogManager.setLogger(new BeaconLoggerAdapter());
            LogManager.setVerboseLoggingEnabled(BuildConfig.DEBUG);
            BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
            beaconManager.adjustSettings(
                    new Settings(
                            BuildConfig.DEBUG,
                            null,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null));
            List<BeaconParser> beaconParsers = beaconManager.getBeaconParsers();
            beaconParsers.add(
                    new BeaconParser("iBeacon")
                            .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
            beaconParsers.add(
                    new BeaconParser("Eddystone UID")
                            .setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        }
    }

    private void initNetworkCallback() {
        // for older versions, we are good with the broadcast receiver configured in the manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                DatabaseLogger.e(
                        TAG, "Unable to get system services, cannot register network callback");
            } else {
                ConnectivityManager.NetworkCallback wifiCallback =
                        NetworkService.getWifiCallback(this);
                if (currentCallback.compareAndSet(null, wifiCallback)) {
                    DatabaseLogger.i(TAG, "Registering callback to await Wi-Fi connection");
                    NetworkRequest request = new NetworkRequest.Builder().build();
                    connectivityManager.registerNetworkCallback(request, wifiCallback);
                }
            }
        }
    }

    private void initBeaconCallback(SharedPreferences preferences) {
        if (supportsBeacons()) {
            Set<String> beacons = preferences.getStringSet(BEACON_LIST, Collections.emptySet());
            if (!beacons.isEmpty()) {
                PresenceBeaconManager.getInstance().initialize(this);
            } else {
                DatabaseLogger.i(
                        TAG, "No beacons configured, not enabling background beacon scanning");
            }
        }
    }

    private void initClientId(SharedPreferences preferences) {
        if (!preferences.contains(MQTT_CLIENT_ID)) {
            DatabaseLogger.i(TAG, "Generating persistent client ID");
            preferences.edit().putString(MQTT_CLIENT_ID, UUID.randomUUID().toString()).apply();
        }
    }

    public void removeConditionCallbacks() {
        removeNetworkCallback();
        PresenceBeaconManager.getInstance().disableScanning();
    }

    private void removeNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                DatabaseLogger.i(TAG, "Removing callback to await Wi-Fi connection");
                ConnectivityManager.NetworkCallback oldCallback = currentCallback.getAndSet(null);
                if (oldCallback != null) {
                    connectivityManager.unregisterNetworkCallback(oldCallback);
                }
            }
        }
    }

    public boolean supportsBeacons() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private void removeOldValues(SharedPreferences preferences) {
        preferences.edit().remove(USE_WORKER_1).remove(CURRENT_WIFI_SSID).apply();
    }
}
