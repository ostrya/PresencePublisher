package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.ostrya.presencepublisher.R;

public class SendOfflineMessagePreference extends AbstractBooleanPreference {
    public static final String SEND_OFFLINE_MESSAGE = "offlinePing";

    public SendOfflineMessagePreference(Context context) {
        super(context, SEND_OFFLINE_MESSAGE, R.string.send_offline_message_title, R.string.send_offline_message_summary);
    }
}
