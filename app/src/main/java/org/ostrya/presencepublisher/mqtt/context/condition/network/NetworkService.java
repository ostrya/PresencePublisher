package org.ostrya.presencepublisher.mqtt.context.condition.network;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.Nullable;

import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (wifiManager == null) {
                DatabaseLogger.e(TAG, "No wifi manager");
            } else {
                ssid = normalizeSsid(wifiManager.getConnectionInfo().getSSID());
            }
        } else {
            if (connectivityManager == null) {
                DatabaseLogger.e(TAG, "Connectivity Manager not found");
            } else {
                //noinspection deprecation
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    //noinspection deprecation
                    ssid = normalizeSsid(activeNetworkInfo.getExtraInfo());
                }
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
            return wifiManager.getConfiguredNetworks();
        } catch (SecurityException e) {
            DatabaseLogger.w(
                    TAG,
                    "Not allowed to get configured networks. As ACCESS_FINE_LOCATION was only added"
                            + " as required in Android Q, this should never happen");
            return Collections.emptyList();
        }
    }
}
