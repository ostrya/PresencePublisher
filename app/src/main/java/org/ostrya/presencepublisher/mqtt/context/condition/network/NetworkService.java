package org.ostrya.presencepublisher.mqtt.context.condition.network;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TransportInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NetworkService {
    private static final String TAG = "NetworkHelper";

    private final ConnectivityManager connectivityManager;
    private final WifiManager wifiManager;

    public NetworkService(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.connectivityManager =
                (ConnectivityManager) applicationContext.getSystemService(CONNECTIVITY_SERVICE);
        this.wifiManager = (WifiManager) applicationContext.getSystemService(WIFI_SERVICE);
    }

    private String normalizeSsid(String ssid) {
        if (ssid == null) {
            return null;
        }
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    @Nullable
    public String getCurrentSsid() {
        String ssid = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (connectivityManager == null) {
                DatabaseLogger.e(TAG, "Connectivity Manager not found");
            } else if (connectivityManager.getActiveNetwork() != null) {
                // hack: since starting with API 31 there is no (non-deprecated) synchronous method
                // to retrieve the Wi-Fi name, we register a NetworkCallback and rely on the fact
                // that it immediately triggers the onCapabilitiesChanged method if the default
                // network is connected (i.e. not null)
                CompletableFuture<WifiInfo> infoFuture = new CompletableFuture<>();
                ConnectivityManager.NetworkCallback callback = new AdHocCallback(infoFuture);
                connectivityManager.registerDefaultNetworkCallback(callback);
                try {
                    // to avoid noticeable delays in the UI, we wait at most 200 ms for the callback
                    // to have been called
                    WifiInfo wifiInfo = infoFuture.get(200, TimeUnit.MILLISECONDS);
                    if (wifiInfo != null) {
                        ssid = normalizeSsid(wifiInfo.getSSID());
                    }
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    DatabaseLogger.w(TAG, "Unable to get SSID within 200 ms", e);
                } finally {
                    connectivityManager.unregisterNetworkCallback(callback);
                }
            } else {
                DatabaseLogger.d(TAG, "Not connected to any network");
            }
        } else {
            if (wifiManager == null) {
                DatabaseLogger.e(TAG, "No wifi manager");
            } else {
                //noinspection deprecation
                ssid = normalizeSsid(wifiManager.getConnectionInfo().getSSID());
            }
        }
        if ("<unknown ssid>".equals(ssid)) {
            return null;
        }
        DatabaseLogger.logDetection("Detected SSID: " + ssid);
        return ssid;
    }

    public List<String> getKnownSsids() {
        List<String> ssids = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            //noinspection deprecation
            for (WifiConfiguration configuration : getConfiguredNetworks()) {
                //noinspection deprecation
                String ssid = normalizeSsid(configuration.SSID);
                if (ssid != null) {
                    ssids.add(ssid);
                }
            }
        }
        if (ssids.isEmpty()) {
            DatabaseLogger.w(TAG, "No known networks found");
            String currentSsid = getCurrentSsid();
            if (currentSsid != null) {
                ssids.add(currentSsid);
            }
        }
        return ssids;
    }

    @SuppressWarnings("deprecation")
    private List<WifiConfiguration> getConfiguredNetworks() {
        if (wifiManager == null) {
            DatabaseLogger.w(TAG, "Unable to get WifiManager");
            return Collections.emptyList();
        }
        try {
            List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
            if (networks == null) {
                return Collections.emptyList();
            }
            return networks;
        } catch (SecurityException e) {
            DatabaseLogger.w(
                    TAG,
                    "Not allowed to get configured networks. As ACCESS_FINE_LOCATION was only added"
                            + " as required in Android Q, this should never happen");
            return Collections.emptyList();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static class AdHocCallback extends ConnectivityManager.NetworkCallback {
        private final CompletableFuture<WifiInfo> infoFuture;

        AdHocCallback(CompletableFuture<WifiInfo> infoFuture) {
            super(ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO);
            this.infoFuture = infoFuture;
        }

        @Override
        public void onCapabilitiesChanged(
                @NonNull Network network, @NonNull NetworkCapabilities capabilities) {
            DatabaseLogger.d(
                    TAG, "Found network " + network + " with capabilities: " + capabilities);
            TransportInfo transportInfo = capabilities.getTransportInfo();
            if (transportInfo instanceof WifiInfo) {
                infoFuture.complete(((WifiInfo) transportInfo));
            } else {
                infoFuture.complete(null);
            }
        }
    }
}
