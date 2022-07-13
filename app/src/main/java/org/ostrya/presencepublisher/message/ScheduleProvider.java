package org.ostrya.presencepublisher.message;

import static org.ostrya.presencepublisher.ui.preference.schedule.ChargingMessageSchedulePreference.CHARGING_MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.MessageSchedulePreference.MESSAGE_SCHEDULE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.VisibleForTesting;
import androidx.preference.PreferenceManager;

public class ScheduleProvider {
    private final SharedPreferences preferences;

    public ScheduleProvider(Context applicationContext) {
        this(PreferenceManager.getDefaultSharedPreferences(applicationContext));
    }

    @VisibleForTesting
    ScheduleProvider(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public long getEstimatedNextTimestamp(long currentTimestamp, boolean isCharging) {
        int minutes = 0;
        if (isCharging) {
            minutes = preferences.getInt(CHARGING_MESSAGE_SCHEDULE, 0);
        }
        if (minutes == 0) {
            minutes = preferences.getInt(MESSAGE_SCHEDULE, 15);
        }
        return currentTimestamp + minutes * 60_000L;
    }
}
