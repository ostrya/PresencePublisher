package org.ostrya.presencepublisher.mqtt.context.condition.network;

import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.util.Function;

import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiresApi(api = Build.VERSION_CODES.M)
public class WifiNetworkHelper {
    private final String TAG = "WifiNetworkHelper";
    private final Map<Long, Optional<String>> ssidByNetworkHandle = new ConcurrentHashMap<>();
    private final WifiEventConsumer consumer;
    private final Function<NetworkCapabilities, Optional<WifiInfo>> wifiResolver;

    public WifiNetworkHelper(
            WifiEventConsumer consumer,
            Function<NetworkCapabilities, Optional<WifiInfo>> wifiResolver) {
        this.consumer = consumer;
        this.wifiResolver = wifiResolver;
    }

    public void handleAvailableNetwork(
            @NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        long networkHandle = network.getNetworkHandle();
        Optional<String> current = NetworkService.getSsid(wifiResolver.apply(networkCapabilities));
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

    public void handleLostNetwork(@NonNull Network network) {
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
}
