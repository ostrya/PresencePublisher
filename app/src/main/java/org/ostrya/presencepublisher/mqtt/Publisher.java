package org.ostrya.presencepublisher.mqtt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.message.Message;
import org.ostrya.presencepublisher.message.battery.BatteryMessageProvider;
import org.ostrya.presencepublisher.message.wifi.WifiMessageProvider;
import org.ostrya.presencepublisher.receiver.AlarmReceiver;
import org.ostrya.presencepublisher.ui.notification.NotificationFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.app.AlarmManager.RTC_WAKEUP;
import static org.ostrya.presencepublisher.Application.ALARM_REQUEST_CODE;
import static org.ostrya.presencepublisher.receiver.AlarmReceiver.ALARM_ACTION;
import static org.ostrya.presencepublisher.ui.preference.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.MessageSchedulePreference.MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.NextScheduleTimestampPreference.NEXT_SCHEDULE;

public class Publisher {
    private static final String TAG = "Publisher";

    private static final int NOTIFICATION_ID = 1;

    private final Context applicationContext;
    private final SharedPreferences sharedPreferences;
    private final WifiMessageProvider wifiMessageProvider;
    private final BatteryMessageProvider batteryMessageProvider;
    private final MqttService mqttService;
    private final PendingIntent scheduledIntent;
    private long lastSuccess;

    public Publisher(Context context) {
        applicationContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        wifiMessageProvider = new WifiMessageProvider(applicationContext);
        batteryMessageProvider = new BatteryMessageProvider(applicationContext);
        mqttService = new MqttService(applicationContext);
        Intent intent = new Intent(applicationContext, AlarmReceiver.class);
        intent.setAction(ALARM_ACTION);
        scheduledIntent = PendingIntent.getBroadcast(applicationContext, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        lastSuccess = sharedPreferences.getLong(LAST_SUCCESS, 0);
    }

    public void publish() {
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
        scheduleFor(System.currentTimeMillis());
    }

    private void scheduleNext() {
        int messageScheduleInMinutes = sharedPreferences.getInt(MESSAGE_SCHEDULE, 15);
        scheduleFor(System.currentTimeMillis() + messageScheduleInMinutes * 60_000L);
    }

    private void scheduleFor(long nextSchedule) {
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
            alarmManager.setAndAllowWhileIdle(RTC_WAKEUP, nextSchedule, scheduledIntent);
        } else {
            alarmManager.set(RTC_WAKEUP, nextSchedule, scheduledIntent);
        }
    }

    private List<Message> getMessagesToSend() {
        List<Message> result = new ArrayList<>();
        result.addAll(wifiMessageProvider.getMessages());
        result.addAll(batteryMessageProvider.getMessages());
        return Collections.unmodifiableList(result);
    }

}
