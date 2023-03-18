package org.ostrya.presencepublisher.preference.condition;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.BooleanPreferenceBase;

public class SendOfflineMessagePreference extends BooleanPreferenceBase {
    public static final String SEND_OFFLINE_MESSAGE = "offlinePing";

    public SendOfflineMessagePreference(Context context) {
        super(
                context,
                SEND_OFFLINE_MESSAGE,
                R.string.send_offline_message_title,
                R.string.send_offline_message_summary);
    }
}
