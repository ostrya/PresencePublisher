package org.ostrya.presencepublisher.message.wifi;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.Nullable;

import com.hypertrack.hyperlog.HyperLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SsidUtil {
    private static final String TAG = "SsidUtil";

    private SsidUtil() {
        // private constructor for helper class
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

    @Nullable
    public static String getCurrentSsid(Context context) {
        Context applicationContext = context.getApplicationContext();
        ConnectivityManager connectivityManager =
                (ConnectivityManager) applicationContext.getSystemService(CONNECTIVITY_SERVICE);
        WifiManager wifiManager = (WifiManager) applicationContext.getSystemService(WIFI_SERVICE);
        String ssid = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (wifiManager == null) {
                HyperLog.e(TAG, "No wifi manager");
            } else {
                ssid = SsidUtil.normalizeSsid(wifiManager.getConnectionInfo().getSSID());
            }
        } else {
            if (connectivityManager == null) {
                HyperLog.e(TAG, "Connectivity Manager not found");
            } else {
                //noinspection deprecation
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    //noinspection deprecation
                    ssid = SsidUtil.normalizeSsid(activeNetworkInfo.getExtraInfo());
                }
            }
        }
        return "<unknown ssid>".equals(ssid) ? null : ssid;
    }

    public static List<String> getKnownSsids(Context context) {
        List<String> ssids = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            //noinspection deprecation
            for (WifiConfiguration configuration : getConfiguredNetworks(context)) {
                //noinspection deprecation
                String ssid = normalizeSsid(configuration.SSID);
                if (ssid != null) {
                    ssids.add(ssid);
                }
            }
        }
        if (ssids.isEmpty()) {
            HyperLog.w(TAG, "No known networks found");
            String currentSsid = getCurrentSsid(context);
            if (currentSsid != null) {
                ssids.add(currentSsid);
            }
        }
        return ssids;
    }

    @SuppressWarnings("deprecation")
    private static List<WifiConfiguration> getConfiguredNetworks(Context context) {
        WifiManager wifiManager =
                (WifiManager)
                        context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
