package org.ostrya.presencepublisher.ui.preference.schedule;

import android.content.Context;
import androidx.preference.SeekBarPreference;
import org.ostrya.presencepublisher.R;

public class ChargingMessageSchedulePreference extends SeekBarPreference {
    public static final String CHARGING_MESSAGE_SCHEDULE = "chargingSchedule";

    public ChargingMessageSchedulePreference(Context context) {
        super(context);
        setKey(CHARGING_MESSAGE_SCHEDULE);
        setMin(0);
        setMax(60);
        setDefaultValue(0);
        setSeekBarIncrement(1);
        setShowSeekBarValue(true);
        setTitle(R.string.message_charging_schedule_title);
        setSummary(R.string.message_charging_schedule_summary);
        setIconSpaceReserved(false);
    }
}
