package org.ostrya.presencepublisher.mqtt.context.condition.network;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.Optional;

@RequiresApi(api = Build.VERSION_CODES.M)
public class WifiCallbackApi23 extends ConnectivityManager.NetworkCallback {
    private final WifiEventConsumer consumer;
    private final WifiManager wifiManager;

    public WifiCallbackApi23(WifiEventConsumer consumer, WifiManager wifiManager) {
        this.consumer = consumer;
        this.wifiManager = wifiManager;
    }

    @Override
    public void onCapabilitiesChanged(
            @NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        checkWifiConnection();
    }

    @Override
    public void onLost(@NonNull Network network) {
        checkWifiConnection();
    }

    // We have no guarantee that what the WifiManager returns is in sync with the/ event (changed /
    // lost) we just received, so let's be on the safe side and only look at what the wifi manager
    // gives us, even if it gives us a wifi network in a 'lost' event.
    // This *should* work most of the time, but to "fix" any race conditions, we also do this check
    // on every publishing schedule
    void checkWifiConnection() {
        Optional<String> ssid = getConnectedSsid();
        Optional<String> bssid = getConnectedBssid();
        if (ssid.isPresent() && bssid.isPresent()) {
            consumer.wifiConnected(ssid.get(), bssid.get());
        } else {
            consumer.wifiDisconnected(null, null);
        }
    }

    private Optional<String> getConnectedSsid() {
        return NetworkService.getSsid(
                Optional.ofNullable(wifiManager.getConnectionInfo())
                        .filter(w -> w.getSupplicantState() == SupplicantState.COMPLETED));
    }

    private Optional<String> getConnectedBssid() {
        return NetworkService.getBssid(
                Optional.ofNullable(wifiManager.getConnectionInfo())
                        .filter(w -> w.getSupplicantState() == SupplicantState.COMPLETED));
    }
}
