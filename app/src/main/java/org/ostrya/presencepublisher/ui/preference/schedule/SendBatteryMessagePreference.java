package org.ostrya.presencepublisher.ui.preference.schedule;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.BooleanPreferenceBase;

public class SendBatteryMessagePreference extends BooleanPreferenceBase {
    public static final String SEND_BATTERY_MESSAGE = "sendbatteryMessage";

    public SendBatteryMessagePreference(Context context) {
        super(
                context,
                SEND_BATTERY_MESSAGE,
                R.string.send_battery_message_title,
                R.string.send_battery_message_summary);
    }
}
