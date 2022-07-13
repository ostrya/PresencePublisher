package org.ostrya.presencepublisher.ui.preference.schedule;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.SeekBarPreference;

import org.ostrya.presencepublisher.R;

public class MessageSchedulePreference extends SeekBarPreference {
    public static final String MESSAGE_SCHEDULE = "ping";

    public MessageSchedulePreference(Context context) {
        super(context);
        setKey(MESSAGE_SCHEDULE);
        setMin(5);
        setMax(60);
        setDefaultValue(15);
        setSeekBarIncrement(1);
        setShowSeekBarValue(true);
        setTitle(R.string.message_schedule_title);
        setSummary(R.string.message_schedule_summary);
        setIconSpaceReserved(false);
        setOnPreferenceChangeListener(
                (prefs, newValue) -> checkMessageSchedule(context, (Integer) newValue));
    }

    private boolean checkMessageSchedule(Context context, int newValue) {
        if (newValue < 15) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.message_schedule_title);
            builder.setMessage(R.string.message_schedule_dialog_text);
            builder.setPositiveButton(
                    R.string.dialog_ok,
                    (dialog, id) -> MessageSchedulePreference.this.setValue(newValue));
            builder.setNegativeButton(
                    R.string.dialog_cancel,
                    (dialog, id) -> {
                        if (MessageSchedulePreference.this.getValue() < 15) {
                            MessageSchedulePreference.this.setValue(15);
                        }
                    });
            builder.create().show();
            return false;
        } else {
            return true;
        }
    }
}
