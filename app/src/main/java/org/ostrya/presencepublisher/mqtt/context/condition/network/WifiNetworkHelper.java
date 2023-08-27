package org.ostrya.presencepublisher.mqtt.context.condition.network;

import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.util.Function;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiresApi(api = Build.VERSION_CODES.M)
public class WifiNetworkHelper {
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
        Optional<String> current = NetworkService.getSsid(wifiResolver.apply(networkCapabilities));
        Optional<String> previous =
                ssidByNetworkHandle.putIfAbsent(network.getNetworkHandle(), current);
        // absent means not a Wi-Fi network, null means (previously) unknown
        //noinspection OptionalAssignedToNull
        if (previous == null && current.isPresent()) {
            consumer.wifiConnected(current.get());
        }
    }

    public void handleLostNetwork(@NonNull Network network) {
        Optional<String> removed = ssidByNetworkHandle.remove(network.getNetworkHandle());
        if (removed != null && removed.isPresent()) {
            consumer.wifiDisconnected(removed.get());
        }
    }
}
