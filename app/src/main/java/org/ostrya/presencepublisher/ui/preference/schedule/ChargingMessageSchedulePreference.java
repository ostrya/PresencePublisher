package org.ostrya.presencepublisher.ui.preference.schedule;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
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
        setOnPreferenceChangeListener(
                (prefs, newValue) -> checkMessageSchedule(context, (Integer) newValue));
    }

    private boolean checkMessageSchedule(Context context, int newValue) {
        if (newValue < 5) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.message_schedule_title);
            builder.setMessage(R.string.charging_message_schedule_dialog_text);
            builder.setPositiveButton(R.string.dialog_ok, (dialog, id) -> setValue(5));
            builder.setNegativeButton(R.string.dialog_cancel, (dialog, id) -> setValue(0));
            builder.create().show();
            return false;
        } else {
            return true;
        }
    }
}
