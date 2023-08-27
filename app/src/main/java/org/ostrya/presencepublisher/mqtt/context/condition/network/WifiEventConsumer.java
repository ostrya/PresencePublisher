package org.ostrya.presencepublisher.mqtt.context.condition.network;

import static org.ostrya.presencepublisher.mqtt.context.condition.network.NetworkService.CURRENT_WIFI_SSID;
import static org.ostrya.presencepublisher.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.schedule.Scheduler;

public class WifiEventConsumer {
    private static final String TAG = "WifiEventConsumer";

    private final SharedPreferences preference;
    private final Scheduler scheduler;

    public WifiEventConsumer(Context applicationContext) {
        preference = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        scheduler = new Scheduler(applicationContext);
    }

    public void wifiDisconnected(String ssid) {
        if (ssid != null) {
            DatabaseLogger.logDetection("Wi-Fi disconnected: " + ssid);
        } else {
            DatabaseLogger.logDetection("Wi-Fi disconnected");
        }
        DatabaseLogger.i(TAG, "Wi-Fi disconnected: " + ssid);
        preference.edit().remove(CURRENT_WIFI_SSID).apply();
        if (preference.getBoolean(SEND_VIA_MOBILE_NETWORK, false)) {
            DatabaseLogger.i(TAG, "Triggering scheduler after disconnect");
            scheduler.runNow();
        }
    }

    public void wifiConnected(String ssid) {
        DatabaseLogger.logDetection("Wi-Fi connected: " + ssid);
        preference.edit().putString(CURRENT_WIFI_SSID, ssid).apply();
        DatabaseLogger.i(TAG, "Triggering scheduler after connecting to Wi-Fi " + ssid);
        scheduler.runNow();
    }
}
