package org.ostrya.presencepublisher.mqtt.context.condition.network;

import static org.ostrya.presencepublisher.log.FormatArgs.args;
import static org.ostrya.presencepublisher.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.common.collect.Sets;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.schedule.Scheduler;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WifiEventConsumer {
    private static final String TAG = "WifiEventConsumer";

    // we use a static reference and do not store the value in preference here because we want to
    // handle app restarts - while the preference value would survive a restart, the reference
    // will be reset so that we do trigger an event after restart even if the current network is the
    // same as before the app restart
    private static final AtomicReference<Set<String>> CURRENT_SSIDS =
            new AtomicReference<>(Collections.emptySet());
    private final SharedPreferences preference;
    private final Scheduler scheduler;

    public WifiEventConsumer(Context applicationContext) {
        preference = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        scheduler = new Scheduler(applicationContext);
    }

    public void wifiUpdated(@NonNull Set<String> allSsids) {
        Set<String> previous = CURRENT_SSIDS.getAndSet(allSsids);
        DatabaseLogger.i(TAG, "Wi-Fi networks updated: %s (was: %s)", args(allSsids, previous));
        Sets.SetView<String> addedSsids = Sets.difference(allSsids, previous);
        Sets.SetView<String> removedSsids = Sets.difference(previous, allSsids);
        if (!addedSsids.isEmpty() || !removedSsids.isEmpty()) {
            removedSsids.forEach(
                    ssid -> DatabaseLogger.logDetection("Wi-Fi disconnected: " + ssid));
            addedSsids.forEach(ssid -> DatabaseLogger.logDetection("Wi-Fi connected: " + ssid));
            if (allSsids.isEmpty()) {
                onDisconnect();
            } else {
                onConnectionChange();
            }
        }
    }

    public void wifiDisconnectedLegacy() {
        Collection<String> previous;
        previous = CURRENT_SSIDS.getAndSet(Collections.emptySet());
        if (!previous.isEmpty()) {
            previous.forEach(
                    ssid -> {
                        DatabaseLogger.logDetection("Wi-Fi disconnected: " + ssid);
                        DatabaseLogger.i(TAG, "Wi-Fi disconnected: %s", args(ssid));
                    });
            onDisconnect();
        } else {
            DatabaseLogger.i(TAG, "Ignoring disconnect, no Wi-Fi connected");
        }
    }

    public void wifiConnectedLegacy(@NonNull String ssid) {
        Collection<String> previous = CURRENT_SSIDS.getAndSet(Collections.singleton(ssid));
        if (!previous.contains(ssid)) {
            DatabaseLogger.logDetection("Wi-Fi connected: " + ssid);
            DatabaseLogger.i(TAG, "Wi-Fi connected: %s", args(ssid));
            onConnectionChange();
        } else {
            DatabaseLogger.i(TAG, "Ignoring connect for already connected Wi-Fi %s", args(ssid));
        }
    }

    private void onDisconnect() {
        if (preference.getBoolean(SEND_VIA_MOBILE_NETWORK, false)) {
            DatabaseLogger.i(TAG, "Triggering scheduler after disconnect");
            // since we don't want to continuously send "offline" / "online" ping-pong, let's
            // wait for some time before sending the message, as the connectivity may have
            // already changed again in the meantime (which would trigger a connected message,
            // thus overriding this disconnect message)
            scheduler.runIn(15, TimeUnit.SECONDS);
        }
    }

    private void onConnectionChange() {
        DatabaseLogger.i(TAG, "Triggering scheduler after Wi-Fi change");
        scheduler.runNow();
    }

    public Set<String> getCurrentSsids() {
        return CURRENT_SSIDS.get();
    }
}
