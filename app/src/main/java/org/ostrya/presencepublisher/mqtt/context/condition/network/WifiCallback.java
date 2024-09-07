package org.ostrya.presencepublisher.mqtt.context.condition.network;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TransportInfo;
import android.net.wifi.WifiInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiresApi(api = Build.VERSION_CODES.S)
public class WifiCallback extends ConnectivityManager.NetworkCallback {
    private static final String TAG = "WifiCallback";
    private final Map<Long, Optional<String>> ssidByNetworkHandle = new ConcurrentHashMap<>();
    private final WifiEventConsumer consumer;

    public WifiCallback(WifiEventConsumer consumer) {
        super(FLAG_INCLUDE_LOCATION_INFO);
        this.consumer = consumer;
    }

    @Override
    public void onCapabilitiesChanged(
            @NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        long networkHandle = network.getNetworkHandle();
        Optional<String> current = NetworkService.getSsid(getWifiInfo(networkCapabilities));
        Optional<String> previous = ssidByNetworkHandle.putIfAbsent(networkHandle, current);
        // absent means not a Wi-Fi network, null means (previously) unknown
        //noinspection OptionalAssignedToNull
        if (previous == null) {
            if (current.isPresent()) {
                DatabaseLogger.d(TAG, "Connected to Wifi network " + networkHandle);
                consumer.wifiConnected(current.get());
            } else {
                DatabaseLogger.d(TAG, "Connected to non-Wifi network " + networkHandle);
            }
        }
    }

    @Override
    public void onLost(@NonNull Network network) {
        long networkHandle = network.getNetworkHandle();
        Optional<String> removed = ssidByNetworkHandle.remove(networkHandle);
        if (removed != null) {
            if (removed.isPresent()) {
                DatabaseLogger.d(TAG, "Lost Wifi network " + networkHandle);
                consumer.wifiDisconnected(removed.get());
            } else {
                DatabaseLogger.d(TAG, "Lost non-Wifi network " + networkHandle);
            }
        }
    }

    private Optional<WifiInfo> getWifiInfo(@NonNull NetworkCapabilities networkCapabilities) {
        TransportInfo transportInfo = networkCapabilities.getTransportInfo();
        if (transportInfo instanceof WifiInfo) {
            return Optional.of((WifiInfo) transportInfo);
        }
        return Optional.empty();
    }
}
