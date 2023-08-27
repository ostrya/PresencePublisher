package org.ostrya.presencepublisher.mqtt.context.condition.network;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.Optional;

@RequiresApi(api = Build.VERSION_CODES.M)
public class WifiCallbackApi23 extends ConnectivityManager.NetworkCallback {
    private final WifiNetworkHelper helper;
    private final WifiManager wifiManager;

    public WifiCallbackApi23(WifiEventConsumer consumer, WifiManager wifiManager) {
        this.helper = new WifiNetworkHelper(consumer, this::getWifiInfo);
        this.wifiManager = wifiManager;
    }

    @Override
    public void onCapabilitiesChanged(
            @NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        helper.handleAvailableNetwork(network, networkCapabilities);
    }

    @Override
    public void onLost(@NonNull Network network) {
        helper.handleLostNetwork(network);
    }

    private Optional<WifiInfo> getWifiInfo(@NonNull NetworkCapabilities unused) {
        return Optional.ofNullable(wifiManager.getConnectionInfo());
    }
}
