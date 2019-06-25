package org.ostrya.presencepublisher.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import com.hypertrack.hyperlog.HyperLog;

import java.util.ArrayList;
import java.util.List;

public class SsidUtil {
    private static final String TAG = "SsidUtil";

    public static String normalizeSsid(final String ssid) {
        if (ssid == null) {
            return null;
        }
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    public static List<String> getKnownSsids(final Context context) {
        List<String> ssids = new ArrayList<>();
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null || wifiManager.getConfiguredNetworks() == null) {
            HyperLog.w(TAG, "No wifi list found");
            return ssids;
        }
        for (WifiConfiguration configuration : wifiManager.getConfiguredNetworks()) {
            ssids.add(normalizeSsid(configuration.SSID));
        }
        return ssids;
    }
}
