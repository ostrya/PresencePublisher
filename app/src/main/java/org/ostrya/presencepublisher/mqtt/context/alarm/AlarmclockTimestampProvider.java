package org.ostrya.presencepublisher.mqtt.context.alarm;

import android.app.AlarmManager;
import android.content.Context;

public class AlarmclockTimestampProvider {
    private final AlarmManager alarmManager;

    public AlarmclockTimestampProvider(Context applicationContext) {
        this.alarmManager =
                (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
    }

    public long getNextAlarmclockTimestamp() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager.AlarmClockInfo nextAlarmClock = alarmManager.getNextAlarmClock();
            if (nextAlarmClock == null) {
                return 0L;
            } else {
                return nextAlarmClock.getTriggerTime();
            }
        } else {
            return 0L;
        }
    }
}
