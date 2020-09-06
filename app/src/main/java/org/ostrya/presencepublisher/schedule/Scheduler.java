package org.ostrya.presencepublisher.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.receiver.AlarmReceiver;
import org.ostrya.presencepublisher.ui.notification.NotificationFactory;

import java.text.DateFormat;
import java.util.Date;

import static android.app.AlarmManager.RTC_WAKEUP;
import static org.ostrya.presencepublisher.Application.ALARM_REQUEST_CODE;
import static org.ostrya.presencepublisher.receiver.AlarmReceiver.ALARM_ACTION;
import static org.ostrya.presencepublisher.ui.preference.schedule.ChargingMessageSchedulePreference.CHARGING_MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.schedule.MessageSchedulePreference.MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;

public class Scheduler {
    private static final String TAG = "Scheduler";

    private static final int NOTIFICATION_ID = 1;

    private final Context applicationContext;
    private final SharedPreferences sharedPreferences;
    private final AlarmManager alarmManager;
    private final PendingIntent scheduledIntent;

    public Scheduler(Context context) {
        applicationContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(applicationContext, AlarmReceiver.class);
        intent.setAction(ALARM_ACTION);
        scheduledIntent = PendingIntent.getBroadcast(applicationContext, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void scheduleNow() {
        scheduleFor(System.currentTimeMillis() + 1_000L, false);
    }

    public void scheduleNext() {
        if (isCharging()) {
            int messageScheduleInMinutes = sharedPreferences.getInt(CHARGING_MESSAGE_SCHEDULE, 0);
            if (messageScheduleInMinutes > 0) {
                scheduleFor(System.currentTimeMillis() + messageScheduleInMinutes * 60_000L, true);
            }
        }
        int messageScheduleInMinutes = sharedPreferences.getInt(MESSAGE_SCHEDULE, 15);
        scheduleFor(System.currentTimeMillis() + messageScheduleInMinutes * 60_000L,
                messageScheduleInMinutes < 15);
    }

    private long getLastSuccess() {
        return sharedPreferences.getLong(LAST_SUCCESS, 0);
    }

    private void scheduleFor(long nextSchedule, boolean ignoreBattery) {
        if (alarmManager == null) {
            HyperLog.e(TAG, "Unable to get alarm manager, cannot schedule!");
            return;
        }
        alarmManager.cancel(scheduledIntent);
        HyperLog.i(TAG, "Next run at " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(nextSchedule)));
        sharedPreferences.edit().putLong(NEXT_SCHEDULE, nextSchedule).apply();
        NotificationManagerCompat.from(applicationContext)
                .notify(NOTIFICATION_ID, NotificationFactory.getNotification(applicationContext, getLastSuccess(), nextSchedule));
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

    private boolean isCharging() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = applicationContext.registerReceiver(null, filter);
        if (batteryStatus == null) {
            return false;
        }
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }
}
