package org.ostrya.presencepublisher.message.wifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.message.Message;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import static org.ostrya.presencepublisher.ui.ConnectionFragment.TOPIC;
import static org.ostrya.presencepublisher.ui.ContentFragment.*;
import static org.ostrya.presencepublisher.ui.ScheduleFragment.*;

public class WifiMessageProvider {
    private static final String TAG = "WifiMessageProvider";

    private final SharedPreferences sharedPreferences;
    private final ConnectivityManager connectivityManager;
    private final WifiManager wifiManager;

    public WifiMessageProvider(Context context) {
        Context applicationContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        connectivityManager = (ConnectivityManager) applicationContext.getSystemService(CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) applicationContext.getSystemService(WIFI_SERVICE);
    }

    public List<Message> getMessages() {
        String topic = sharedPreferences.getString(TOPIC, null);
        if (topic == null) {
            HyperLog.w(TAG, "No topic defined, not generating any messages");
            return Collections.emptyList();
        }
        Message.MessageBuilder messageBuilder = Message.messageForTopic(topic);
        boolean connectedToWiFi = isConnectedToWiFi();
        if (connectedToWiFi) {
            String ssid = getSsidIfMatching();
            if (ssid != null) {
                HyperLog.i(TAG, "Scheduling message for SSID " + ssid);
                String onlineContent = sharedPreferences.getString(WIFI_PREFIX + ssid, DEFAULT_CONTENT_ONLINE);
                return Collections.singletonList(messageBuilder.withContent(onlineContent));
            }
        }
        if (sharedPreferences.getBoolean(OFFLINE_PING, false)
                && (connectedToWiFi || sharedPreferences.getBoolean(MOBILE_NETWORK_PING, false))) {
            HyperLog.i(TAG, "Scheduling offline message");
            String offlineContent = sharedPreferences.getString(CONTENT_OFFLINE, DEFAULT_CONTENT_OFFLINE);
            return Collections.singletonList(messageBuilder.withContent(offlineContent));
        }
        return Collections.emptyList();
    }


    private boolean isConnectedToWiFi() {
        if (connectivityManager == null) {
            HyperLog.e(TAG, "Connectivity Manager not found");
            return false;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null
                || !activeNetworkInfo.isConnected()) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            for (Network network : connectivityManager.getAllNetworks()) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true;
                }
            }
            return false;
        } else {
            //noinspection deprecation
            return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
    }

    private String getSsidIfMatching() {
        HyperLog.i(TAG, "Checking SSID");
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
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    ssid = SsidUtil.normalizeSsid(activeNetworkInfo.getExtraInfo());
                }
            }
        }
        if (ssid == null) {
            HyperLog.i(TAG, "No SSID found");
            return null;
        }
        Set<String> targetSsids = sharedPreferences.getStringSet(SSID_LIST, Collections.emptySet());
        if (targetSsids.contains(ssid)) {
            HyperLog.d(TAG, "Correct network found");
            return ssid;
        } else {
            HyperLog.i(TAG, "'" + ssid + "' does not match any desired network '" + targetSsids + "', skipping.");
            return null;
        }
    }
}
