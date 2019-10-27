package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.ostrya.presencepublisher.R;

public class NextScheduleTimestampPreference extends AbstractTimestampPreference {
    public static final String NEXT_SCHEDULE = "nextPing";

    public NextScheduleTimestampPreference(Context context) {
        super(context, NEXT_SCHEDULE, R.string.next_schedule_title);
    }
}
