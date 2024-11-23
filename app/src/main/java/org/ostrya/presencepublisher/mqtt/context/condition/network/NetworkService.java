package org.ostrya.presencepublisher.mqtt.context.condition.network;

import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NetworkService {
    private static final String TAG = "NetworkService";

    private final WifiManager wifiManager;
    private final WifiEventConsumer wifiEventConsumer;
    @Nullable private final WifiCallbackApi23 wifiCallbackApi23;

    public NetworkService(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.wifiManager = (WifiManager) applicationContext.getSystemService(WIFI_SERVICE);
        this.wifiEventConsumer = new WifiEventConsumer(applicationContext);
        this.wifiCallbackApi23 =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                && Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                        ? new WifiCallbackApi23(wifiEventConsumer, wifiManager)
                        : null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static ConnectivityManager.NetworkCallback getWifiCallback(Context context) {
        Context applicationContext = context.getApplicationContext();
        WifiEventConsumer consumer = new WifiEventConsumer(applicationContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new WifiCallback(consumer);
        } else {
            WifiManager wifiManager =
                    (WifiManager) applicationContext.getSystemService(WIFI_SERVICE);
            return new WifiCallbackApi23(consumer, wifiManager);
        }
    }

    @Nullable
    public String getCurrentSsid() {
        // since the detection of wifi networks in Android < 12 with a network callback is flaky
        // due to the SSID not being present in the event data, we re-check the current wifi
        // whenever we need it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && wifiCallbackApi23 != null) {
            wifiCallbackApi23.checkWifiConnection();
        }
        return wifiEventConsumer.getCurrentSsid();
    }

    @Nullable
    public String getCurrentBssid() {
        // since the detection of wifi networks in Android < 12 with a network callback is flaky
        // due to the SSID not being present in the event data, we re-check the current wifi
        // whenever we need it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && wifiCallbackApi23 != null) {
            wifiCallbackApi23.checkWifiConnection();
        }
        return wifiEventConsumer.getCurrentBssid();
    }

    public static Optional<String> getSsid(Optional<WifiInfo> wifiInfo) {
        return wifiInfo.map(WifiInfo::getSSID).map(NetworkService::normalizeSsid);
    }

    public static Optional<String> getBssid(Optional<WifiInfo> wifiInfo) {
        return wifiInfo.map(WifiInfo::getBSSID);
    }

    private static String normalizeSsid(String ssid) {
        if (ssid == null) {
            return null;
        }
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid.substring(1, ssid.length() - 1);
        }
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
}
