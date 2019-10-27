package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

public class HostPreference extends AbstractTextPreference {
    public static final String HOST = "host";

    public HostPreference(Context context) {
        super(context, HOST, new RegexValidator("[^:/]+"), R.string.host_title, R.string.host_summary);
    }
}
