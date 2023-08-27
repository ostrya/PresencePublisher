package org.ostrya.presencepublisher.mqtt.context.condition.network;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TransportInfo;
import android.net.wifi.WifiInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.Optional;

@RequiresApi(api = Build.VERSION_CODES.S)
public class WifiCallback extends ConnectivityManager.NetworkCallback {
    private final WifiNetworkHelper helper;

    public WifiCallback(WifiEventConsumer consumer) {
        super(FLAG_INCLUDE_LOCATION_INFO);
        helper = new WifiNetworkHelper(consumer, this::getWifiInfo);
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

    private Optional<WifiInfo> getWifiInfo(@NonNull NetworkCapabilities networkCapabilities) {
        TransportInfo transportInfo = networkCapabilities.getTransportInfo();
        if (transportInfo instanceof WifiInfo) {
            return Optional.of((WifiInfo) transportInfo);
        }
        return Optional.empty();
    }
}
