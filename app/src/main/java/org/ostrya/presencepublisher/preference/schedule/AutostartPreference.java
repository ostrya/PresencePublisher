package org.ostrya.presencepublisher.preference.schedule;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.BooleanPreferenceBase;

public class AutostartPreference extends BooleanPreferenceBase {
    public static final String AUTOSTART = "autostart";

    public AutostartPreference(Context context) {
        super(context, AUTOSTART, R.string.autostart_title, R.string.autostart_summary);
    }
}
