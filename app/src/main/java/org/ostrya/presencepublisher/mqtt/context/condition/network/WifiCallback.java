package org.ostrya.presencepublisher.mqtt.context.condition.network;

import static org.ostrya.presencepublisher.log.FormatArgs.args;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TransportInfo;
import android.net.wifi.WifiInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RequiresApi(api = Build.VERSION_CODES.S)
public class WifiCallback extends ConnectivityManager.NetworkCallback implements AutoCloseable {
    private static final String TAG = "WifiCallback";
    private final Map<Long, String> ssidByNetworkHandle = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<Set<String>> lastSeenSsids = new AtomicReference<>();
    private final WifiEventConsumer consumer;
    private ScheduledFuture<?> pendingTask;

    public WifiCallback(WifiEventConsumer consumer) {
        super(FLAG_INCLUDE_LOCATION_INFO);
        this.consumer = consumer;
    }

    @Override
    public void onCapabilitiesChanged(
            @NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        long networkHandle = network.getNetworkHandle();
        Optional<String> current = NetworkService.getSsid(getWifiInfo(networkCapabilities));
        if (current.isPresent()) {
            String ssid = current.get();
            String previous = ssidByNetworkHandle.putIfAbsent(networkHandle, ssid);
            // we only care about new networks
            if (previous == null) {
                DatabaseLogger.i(TAG, "Connected to Wi-Fi network %s", args(ssid));
                scheduleDelayedEvent();
            } else {
                DatabaseLogger.d(TAG, "Ignored capability change of Wi-Fi network %s", args(ssid));
            }
        } else {
            DatabaseLogger.w(TAG, "SSID of network could not be determined");
        }
    }

    @Override
    public void onLost(@NonNull Network network) {
        long networkHandle = network.getNetworkHandle();
        String removed = ssidByNetworkHandle.remove(networkHandle);
        if (removed != null) {
            DatabaseLogger.i(TAG, "Lost Wi-Fi network %s", args(removed));
            scheduleDelayedEvent();
        } else {
            DatabaseLogger.w(TAG, "Ignore disconnect of unknown network");
        }
    }

    @Override
    public void onUnavailable() {
        DatabaseLogger.w(TAG, "Callback became unavailable");
    }

    private Optional<WifiInfo> getWifiInfo(@NonNull NetworkCapabilities networkCapabilities) {
        TransportInfo transportInfo = networkCapabilities.getTransportInfo();
        if (transportInfo instanceof WifiInfo) {
            return Optional.of((WifiInfo) transportInfo);
        }
        return Optional.empty();
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }

    // Since connect and disconnect may appear close together, we delay sending the event for 5 s
    // and remember the state before the first event happened. This way, we should ideally only get
    // a single event containing the sum of all changes.
    private void scheduleDelayedEvent() {
        Set<String> previous = lastSeenSsids.getAndSet(new HashSet<>(ssidByNetworkHandle.values()));
        if (previous == null) {
            DatabaseLogger.d(TAG, "Scheduling change event for changed SSIDs");
        } else {
            DatabaseLogger.d(
                    TAG,
                    "Delaying change event, ignored previously seen SSIDs: %s",
                    args(previous));
        }
        synchronized (lastSeenSsids) {
            if (pendingTask != null && !pendingTask.isDone()) {
                pendingTask.cancel(false);
            }
            pendingTask =
                    scheduler.schedule(
                            () -> {
                                Set<String> ssids = lastSeenSsids.getAndSet(null);
                                if (ssids != null) {
                                    consumer.wifiUpdated(ssids);
                                }
                            },
                            5,
                            TimeUnit.SECONDS);
        }
    }
}
