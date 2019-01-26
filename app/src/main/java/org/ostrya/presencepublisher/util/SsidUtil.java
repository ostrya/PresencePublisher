package org.ostrya.presencepublisher.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SsidUtil {
    private static final String TAG = SsidUtil.class.getSimpleName();

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
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Log.w(TAG, "No wifi manager found");
            return null;
        }
        List<String> ssids = new ArrayList<>();
        for (WifiConfiguration configuration : wifiManager.getConfiguredNetworks()) {
            ssids.add(normalizeSsid(configuration.SSID));
        }
        return ssids;
    }
}
