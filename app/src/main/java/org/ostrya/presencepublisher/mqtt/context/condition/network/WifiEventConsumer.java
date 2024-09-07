package org.ostrya.presencepublisher.mqtt.context.condition.network;

import static org.ostrya.presencepublisher.mqtt.context.condition.network.NetworkService.CURRENT_WIFI_SSID;
import static org.ostrya.presencepublisher.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.schedule.Scheduler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WifiEventConsumer {
    private static final String TAG = "WifiEventConsumer";

    private static final AtomicReference<String> CURRENT_SSID = new AtomicReference<>();
    private final SharedPreferences preference;
    private final Scheduler scheduler;

    public WifiEventConsumer(Context applicationContext) {
        preference = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        scheduler = new Scheduler(applicationContext);
    }

    public void wifiDisconnected(@Nullable String ssid) {
        if (CURRENT_SSID.getAndSet(null) != null) {
            if (ssid != null) {
                DatabaseLogger.logDetection("Wi-Fi disconnected: " + ssid);
            } else {
                DatabaseLogger.logDetection("Wi-Fi disconnected");
            }
            DatabaseLogger.i(TAG, "Wi-Fi disconnected: " + ssid);
            preference.edit().remove(CURRENT_WIFI_SSID).apply();
            if (preference.getBoolean(SEND_VIA_MOBILE_NETWORK, false)) {
                DatabaseLogger.i(TAG, "Triggering scheduler after disconnect");
                // since we don't want to continuously send "offline" / "online" ping-pong, let's
                // wait
                // for some time before sending the message, as the connectivity may have already
                // changed again in the meantime (which would trigger a connected message, thus
                // overriding this disconnect message)
                scheduler.runIn(15, TimeUnit.SECONDS);
            }
        } else {
            DatabaseLogger.d(TAG, "Ignoring disconnect for already disconnected network " + ssid);
        }
    }

    public void wifiConnected(@NonNull String ssid) {
        // we compare to a static reference and not to the value in preference here because we want
        // handle app restarts - while the preference value would survive a restart, the reference
        // should be reset so that we do trigger an event after restart even if the current network
        // is the same as before the app restart
        if (!ssid.equals(CURRENT_SSID.getAndSet(ssid))) {
            DatabaseLogger.logDetection("Wi-Fi connected: " + ssid);
            preference.edit().putString(CURRENT_WIFI_SSID, ssid).apply();
            DatabaseLogger.i(TAG, "Triggering scheduler after connecting to Wi-Fi " + ssid);
            scheduler.runNow();
        } else {
            DatabaseLogger.d(TAG, "Ignoring connect for already connected network " + ssid);
        }
    }
}
