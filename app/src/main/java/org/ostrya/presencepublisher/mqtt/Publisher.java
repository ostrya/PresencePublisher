package org.ostrya.presencepublisher.mqtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.message.BatteryMessageProvider;
import org.ostrya.presencepublisher.message.BeaconMessageProvider;
import org.ostrya.presencepublisher.message.Message;
import org.ostrya.presencepublisher.message.OfflineMessageProvider;
import org.ostrya.presencepublisher.message.WifiMessageProvider;
import org.ostrya.presencepublisher.schedule.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;
import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;

public class Publisher {
    private static final String TAG = "Publisher";

    private final SharedPreferences sharedPreferences;
    private final BatteryMessageProvider batteryMessageProvider;
    private final BeaconMessageProvider beaconMessageProvider;
    private final OfflineMessageProvider offlineMessageProvider;
    private final WifiMessageProvider wifiMessageProvider;
    private final MqttService mqttService;
    private final ConnectivityManager connectivityManager;
    private final Scheduler scheduler;

    public Publisher(Context context) {
        Context applicationContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        batteryMessageProvider = new BatteryMessageProvider(applicationContext);
        beaconMessageProvider = new BeaconMessageProvider(applicationContext);
        offlineMessageProvider = new OfflineMessageProvider(applicationContext);
        wifiMessageProvider = new WifiMessageProvider(applicationContext);
        mqttService = new MqttService(applicationContext);
        connectivityManager = (ConnectivityManager) applicationContext.getSystemService(CONNECTIVITY_SERVICE);
        scheduler = new Scheduler(applicationContext);
    }

    public void publish() {
        if (!sendMessageViaCurrentConnection()) {
            HyperLog.i(TAG, "Not connected to valid network, waiting for re-connect");
            scheduler.waitForNetworkReconnect();
            return;
        }
        try {
            List<Message> messages = getMessagesToSend();
            if (!messages.isEmpty()) {
                doSend(messages);
            }
        } catch (RuntimeException e) {
            HyperLog.w(TAG, "Error while getting messages to send", e);
        } finally {
            scheduler.scheduleNext();
        }
    }

    private void doSend(List<Message> messages) {
        HyperLog.d(TAG, "Sending messages");
        try {
            mqttService.sendMessages(messages);
            sharedPreferences.edit().putLong(LAST_SUCCESS, System.currentTimeMillis()).apply();
        } catch (Exception e) {
            HyperLog.w(TAG, "Error while sending messages", e);
        }
    }

    private List<Message> getMessagesToSend() {
        List<Message> result = new ArrayList<>(wifiMessageProvider.getMessages());
        result.addAll(beaconMessageProvider.getMessages());
        if (result.isEmpty() && sharedPreferences.getBoolean(SEND_OFFLINE_MESSAGE, false)) {
            result.addAll(offlineMessageProvider.getMessages());
        }
        if (!result.isEmpty()) {
            result.addAll(batteryMessageProvider.getMessages());
        }
        return Collections.unmodifiableList(result);
    }

    private boolean sendMessageViaCurrentConnection() {
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
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
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
}
