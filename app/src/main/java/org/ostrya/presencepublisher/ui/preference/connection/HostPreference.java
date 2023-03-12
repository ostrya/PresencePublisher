package org.ostrya.presencepublisher.ui.preference.connection;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.ui.util.HostValidator;

public class HostPreference extends TextPreferenceBase {
    public static final String HOST = "host";

    public HostPreference(Context context) {
        super(context, HOST, new HostValidator(), R.string.host_title, R.string.host_summary);
    }
}
