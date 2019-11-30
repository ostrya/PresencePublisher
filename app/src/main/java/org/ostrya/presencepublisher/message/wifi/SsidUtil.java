package org.ostrya.presencepublisher.message.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import androidx.annotation.Nullable;
import com.hypertrack.hyperlog.HyperLog;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class SsidUtil {
    private static final String TAG = "SsidUtil";

    private static String normalizeSsid(final String ssid) {
        if (ssid == null) {
            return null;
        }
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    @Nullable
    public static String getCurrentSsid(final Context context) {
        Context applicationContext = context.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext.getSystemService(CONNECTIVITY_SERVICE);
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

    public static List<String> getKnownSsids(final Context context) {
        List<String> ssids = new ArrayList<>();
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            HyperLog.w(TAG, "Unable to get WifiManager");
            return ssids;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            //noinspection deprecation
            List<WifiConfiguration> configuredNetworks = null;
            try {
                //noinspection deprecation
                configuredNetworks = wifiManager.getConfiguredNetworks();
            } catch (SecurityException e) {
                HyperLog.w(TAG, "Not allowed to get configured networks. " +
                        "As ACCESS_FINE_LOCATION was only added as required in Android Q, this should never happen");
            }
            if (configuredNetworks == null) {
                HyperLog.w(TAG, "No wifi list found");
            } else {
                //noinspection deprecation
                for (WifiConfiguration configuration : configuredNetworks) {
                    //noinspection deprecation
                    String ssid = normalizeSsid(configuration.SSID);
                    if (ssid != null) {
                        ssids.add(ssid);
                    }
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
}
