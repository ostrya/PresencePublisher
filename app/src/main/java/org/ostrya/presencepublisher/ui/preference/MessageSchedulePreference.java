package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import androidx.preference.SeekBarPreference;
import org.ostrya.presencepublisher.R;

public class MessageSchedulePreference extends SeekBarPreference {
    public static final String MESSAGE_SCHEDULE = "ping";

    public MessageSchedulePreference(Context context) {
        super(context);
        setKey(MESSAGE_SCHEDULE);
        setMin(1);
        setMax(30);
        setDefaultValue(15);
        setSeekBarIncrement(1);
        setShowSeekBarValue(true);
        setTitle(R.string.message_schedule_title);
        setSummary(R.string.message_schedule_summary);
        setIconSpaceReserved(false);
    }
}
