package org.ostrya.presencepublisher.preference.connection;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.preference.common.validation.HostValidator;

public class HostPreference extends TextPreferenceBase {
    public static final String HOST = "host";

    public HostPreference(Context context) {
        super(context, HOST, new HostValidator(), R.string.host_title, R.string.host_summary);
    }
}
