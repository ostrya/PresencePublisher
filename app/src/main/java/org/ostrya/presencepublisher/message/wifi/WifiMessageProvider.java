package org.ostrya.presencepublisher.message.wifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.message.Message;
import org.ostrya.presencepublisher.ui.preference.OfflineContentPreference;
import org.ostrya.presencepublisher.ui.preference.WifiNetworkPreference;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static org.ostrya.presencepublisher.ui.preference.AddNetworkChoicePreferenceDummy.SSID_LIST;
import static org.ostrya.presencepublisher.ui.preference.PresenceTopicPreference.PRESENCE_TOPIC;
import static org.ostrya.presencepublisher.ui.preference.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;

public class WifiMessageProvider {
    private static final String TAG = "WifiMessageProvider";

    private final Context applicationContext;
    private final SharedPreferences sharedPreferences;
    private final ConnectivityManager connectivityManager;

    public WifiMessageProvider(Context context) {
        applicationContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        connectivityManager = (ConnectivityManager) applicationContext.getSystemService(CONNECTIVITY_SERVICE);
    }

    public List<Message> getMessages() {
        String topic = sharedPreferences.getString(PRESENCE_TOPIC, null);
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
                String onlineContent = sharedPreferences.getString(WifiNetworkPreference.WIFI_CONTENT_PREFIX + ssid, WifiNetworkPreference.DEFAULT_CONTENT_ONLINE);
                return Collections.singletonList(messageBuilder.withContent(onlineContent));
            }
        }
        if (sharedPreferences.getBoolean(SEND_OFFLINE_MESSAGE, false)
                && (connectedToWiFi || sharedPreferences.getBoolean(SEND_VIA_MOBILE_NETWORK, false))) {
            HyperLog.i(TAG, "Scheduling offline message");
            String offlineContent = sharedPreferences.getString(OfflineContentPreference.OFFLINE_CONTENT, OfflineContentPreference.DEFAULT_CONTENT_OFFLINE);
            return Collections.singletonList(messageBuilder.withContent(offlineContent));
        }
        return Collections.emptyList();
    }


    private boolean isConnectedToWiFi() {
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
            return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
                    || activeNetworkInfo.getType() == ConnectivityManager.TYPE_VPN
                    || activeNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET;
        }
    }

    private String getSsidIfMatching() {
        HyperLog.i(TAG, "Checking SSID");
        String ssid = SsidUtil.getCurrentSsid(applicationContext);
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
