package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.ostrya.presencepublisher.R;

public class AutostartPreference extends AbstractBooleanPreference {
    public static final String AUTOSTART = "autostart";

    public AutostartPreference(Context context) {
        super(context, AUTOSTART, R.string.autostart_title, R.string.autostart_summary);
    }
}
