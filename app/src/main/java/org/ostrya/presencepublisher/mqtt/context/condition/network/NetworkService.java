package org.ostrya.presencepublisher.mqtt.context.condition.network;

import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NetworkService {
    private static final String TAG = "NetworkService";

    static final String CURRENT_WIFI_SSID = "currentWifiSsid";

    private final WifiManager wifiManager;
    private final SharedPreferences preference;

    public NetworkService(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.wifiManager = (WifiManager) applicationContext.getSystemService(WIFI_SERVICE);
        this.preference = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static ConnectivityManager.NetworkCallback getWifiCallback(Context context) {
        Context applicationContext = context.getApplicationContext();
        WifiEventConsumer consumer = new WifiEventConsumer(applicationContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new WifiCallback(consumer);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new WifiCallbackApi29(consumer);
        } else {
            WifiManager wifiManager =
                    (WifiManager) applicationContext.getSystemService(WIFI_SERVICE);
            return new WifiCallbackApi23(consumer, wifiManager);
        }
    }

    @Nullable
    public String getCurrentSsid() {
        return preference.getString(CURRENT_WIFI_SSID, null);
    }

    public static Optional<String> getSsid(Optional<WifiInfo> wifiInfo) {
        return wifiInfo.map(WifiInfo::getSSID).map(NetworkService::normalizeSsid);
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
