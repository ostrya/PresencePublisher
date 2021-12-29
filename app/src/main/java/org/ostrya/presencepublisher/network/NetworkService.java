package org.ostrya.presencepublisher.network;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import static org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.Nullable;

import com.hypertrack.hyperlog.HyperLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkService {
    private static final String TAG = "NetworkHelper";

    private final SharedPreferences sharedPreferences;
    private final ConnectivityManager connectivityManager;
    private final WifiManager wifiManager;

    public NetworkService(Context context, SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        Context applicationContext = context.getApplicationContext();
        this.connectivityManager =
                (ConnectivityManager) applicationContext.getSystemService(CONNECTIVITY_SERVICE);
        this.wifiManager = (WifiManager) applicationContext.getSystemService(WIFI_SERVICE);
    }

    public boolean sendMessageViaCurrentConnection() {
        if (connectivityManager == null) {
            HyperLog.e(TAG, "Connectivity Manager not found");
            return false;
        }
        //noinspection deprecation
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        //noinspection deprecation
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (Network network : connectivityManager.getAllNetworks()) {
                NetworkCapabilities networkCapabilities =
                        connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null
                        && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true;
                }
            }
            return sendViaMobile();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
                    || activeNetworkInfo.getType() == ConnectivityManager.TYPE_VPN
                    || activeNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
                    || sendViaMobile();
        } else {
            //noinspection deprecation
            return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
                    || activeNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
                    || sendViaMobile();
        }
    }

    private boolean sendViaMobile() {
        return sharedPreferences.getBoolean(SEND_VIA_MOBILE_NETWORK, false);
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
                HyperLog.e(TAG, "No wifi manager");
            } else {
                ssid = normalizeSsid(wifiManager.getConnectionInfo().getSSID());
            }
        } else {
            if (connectivityManager == null) {
                HyperLog.e(TAG, "Connectivity Manager not found");
            } else {
                //noinspection deprecation
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    //noinspection deprecation
                    ssid = normalizeSsid(activeNetworkInfo.getExtraInfo());
                }
            }
        }
        return "<unknown ssid>".equals(ssid) ? null : ssid;
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
            HyperLog.w(TAG, "No known networks found");
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
            HyperLog.w(TAG, "Unable to get WifiManager");
            return Collections.emptyList();
        }
        try {
            return wifiManager.getConfiguredNetworks();
        } catch (SecurityException e) {
            HyperLog.w(
                    TAG,
                    "Not allowed to get configured networks. As ACCESS_FINE_LOCATION was only added"
                            + " as required in Android Q, this should never happen");
            return Collections.emptyList();
        }
    }
}
