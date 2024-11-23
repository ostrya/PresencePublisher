package org.ostrya.presencepublisher.mqtt.context.condition.network;

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

    // we use a static reference and do not store the value in preference here because we want to
    // handle app restarts - while the preference value would survive a restart, the reference
    // will be reset so that we do trigger an event after restart even if the current network is the
    // same as before the app restart
    private static final AtomicReference<String> CURRENT_SSID = new AtomicReference<>();
    private static final AtomicReference<String> CURRENT_BSSID = new AtomicReference<>();
    private final SharedPreferences preference;
    private final Scheduler scheduler;

    public WifiEventConsumer(Context applicationContext) {
        preference = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        scheduler = new Scheduler(applicationContext);
    }

    public void wifiDisconnected(@Nullable String ssid, @Nullable String bssid) {
        if (CURRENT_SSID.getAndSet(null) != null || CURRENT_BSSID.getAndSet(null) != null) {
            if (ssid != null) {
                DatabaseLogger.logDetection("Wi-Fi disconnected: " + ssid + " " + bssid);
            } else {
                DatabaseLogger.logDetection("Wi-Fi disconnected");
            }
            DatabaseLogger.i(TAG, "Wi-Fi disconnected: " + ssid + " " + bssid);
            if (preference.getBoolean(SEND_VIA_MOBILE_NETWORK, false)) {
                DatabaseLogger.i(TAG, "Triggering scheduler after disconnect");
                // since we don't want to continuously send "offline" / "online" ping-pong, let's
                // wait for some time before sending the message, as the connectivity may have
                // already changed again in the meantime (which would trigger a connected message,
                // thus overriding this disconnect message)
                scheduler.runIn(15, TimeUnit.SECONDS);
            }
        } else {
            DatabaseLogger.d(TAG, "Ignoring disconnect for already disconnected network " + ssid + " " + bssid);
        }
    }

    public void wifiConnected(@NonNull String ssid, @NonNull String bssid) {
        if (!ssid.equals(CURRENT_SSID.getAndSet(ssid)) && !bssid.equals(CURRENT_BSSID.getAndSet(bssid))) {
            DatabaseLogger.logDetection("Wi-Fi connected/changed: " + ssid + " " + bssid);
            DatabaseLogger.i(TAG, "Triggering scheduler after connecting to Wi-Fi " + ssid + " " + bssid);
            scheduler.runNow();
        } else {
            DatabaseLogger.d(TAG, "Ignoring connect for already connected network " + ssid + " " + bssid);
        }
    }

    @Nullable
    public String getCurrentSsid() {
        return CURRENT_SSID.get();
    }

    @Nullable
    public String getCurrentBssid() {
        return CURRENT_BSSID.get();
    }
}
