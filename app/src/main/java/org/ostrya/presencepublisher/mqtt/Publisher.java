package org.ostrya.presencepublisher.mqtt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.message.BatteryMessageProvider;
import org.ostrya.presencepublisher.message.BeaconMessageProvider;
import org.ostrya.presencepublisher.message.Message;
import org.ostrya.presencepublisher.message.OfflineMessageProvider;
import org.ostrya.presencepublisher.message.WifiMessageProvider;
import org.ostrya.presencepublisher.receiver.AlarmReceiver;
import org.ostrya.presencepublisher.ui.notification.NotificationFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static org.ostrya.presencepublisher.Application.ALARM_REQUEST_CODE;
import static org.ostrya.presencepublisher.receiver.AlarmReceiver.ALARM_ACTION;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;
import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.schedule.MessageSchedulePreference.MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;

public class Publisher {
    private static final String TAG = "Publisher";

    private static final int NOTIFICATION_ID = 1;

    private final Context applicationContext;
    private final SharedPreferences sharedPreferences;
    private final BatteryMessageProvider batteryMessageProvider;
    private final BeaconMessageProvider beaconMessageProvider;
    private final OfflineMessageProvider offlineMessageProvider;
    private final WifiMessageProvider wifiMessageProvider;
    private final MqttService mqttService;
    private final PendingIntent scheduledIntent;
    private final ConnectivityManager connectivityManager;
    private long lastSuccess;

    public Publisher(Context context) {
        applicationContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        batteryMessageProvider = new BatteryMessageProvider(applicationContext);
        beaconMessageProvider = new BeaconMessageProvider(applicationContext);
        offlineMessageProvider = new OfflineMessageProvider(applicationContext);
        wifiMessageProvider = new WifiMessageProvider(applicationContext);
        mqttService = new MqttService(applicationContext);
        Intent intent = new Intent(applicationContext, AlarmReceiver.class);
        intent.setAction(ALARM_ACTION);
        scheduledIntent = PendingIntent.getBroadcast(applicationContext, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        connectivityManager = (ConnectivityManager) applicationContext.getSystemService(CONNECTIVITY_SERVICE);
        lastSuccess = sharedPreferences.getLong(LAST_SUCCESS, 0);
    }

    public void publish() {
        if (!sendMessageViaCurrentConnection()) {
            HyperLog.i(TAG, "Not connected to valid network, not sending messages");
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
            scheduleNext();
        }
    }

    private void doSend(List<Message> messages) {
        HyperLog.d(TAG, "Sending messages");
        try {
            mqttService.sendMessages(messages);
            lastSuccess = System.currentTimeMillis();
            sharedPreferences.edit().putLong(LAST_SUCCESS, lastSuccess).apply();
        } catch (Exception e) {
            HyperLog.w(TAG, "Error while sending messages", e);
        }
    }

    public void scheduleNow() {
        scheduleFor(System.currentTimeMillis(), false);
    }

    private void scheduleNext() {
        int messageScheduleInMinutes = sharedPreferences.getInt(MESSAGE_SCHEDULE, 15);
        scheduleFor(System.currentTimeMillis() + messageScheduleInMinutes * 60_000L,
                messageScheduleInMinutes < 15);
    }

    private void scheduleFor(long nextSchedule, boolean ignoreBattery) {
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            HyperLog.e(TAG, "Unable to get alarm manager, cannot schedule!");
            return;
        }
        alarmManager.cancel(scheduledIntent);
        HyperLog.i(TAG, "Next run at " + new Date(nextSchedule));
        sharedPreferences.edit().putLong(NEXT_SCHEDULE, nextSchedule).apply();
        NotificationManagerCompat.from(applicationContext)
                .notify(NOTIFICATION_ID, NotificationFactory.getNotification(applicationContext, lastSuccess, nextSchedule));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ignoreBattery) {
                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(nextSchedule, scheduledIntent), scheduledIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(RTC_WAKEUP, nextSchedule, scheduledIntent);
            }
        } else {
            alarmManager.set(RTC_WAKEUP, nextSchedule, scheduledIntent);
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            for (Network network : connectivityManager.getAllNetworks()) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true;
                }
            }
            return sendViaMobile();
        } else {
            //noinspection deprecation
            return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
                    || activeNetworkInfo.getType() == ConnectivityManager.TYPE_VPN
                    || activeNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
                    || sendViaMobile();
        }
    }

    private boolean sendViaMobile() {
        return sharedPreferences.getBoolean(SEND_VIA_MOBILE_NETWORK, false);
    }
}
