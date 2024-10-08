package org.ostrya.presencepublisher.mqtt.context.device;

import android.app.AlarmManager;
import android.content.Context;

public class AlarmclockTimestampProvider {
    private final AlarmManager alarmManager;

    public AlarmclockTimestampProvider(Context applicationContext) {
        this.alarmManager =
                (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
    }

    public long getNextAlarmclockTimestamp() {
        AlarmManager.AlarmClockInfo nextAlarmClock = alarmManager.getNextAlarmClock();
        if (nextAlarmClock == null) {
            return 0L;
        } else {
            return nextAlarmClock.getTriggerTime();
        }
    }
}
