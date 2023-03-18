package org.ostrya.presencepublisher.preference.schedule;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.TimestampPreferenceBase;

public class NextScheduleTimestampPreference extends TimestampPreferenceBase {
    public static final String NEXT_SCHEDULE = "nextPing";

    public NextScheduleTimestampPreference(Context context) {
        super(context, NEXT_SCHEDULE, R.string.next_schedule_title);
    }
}
