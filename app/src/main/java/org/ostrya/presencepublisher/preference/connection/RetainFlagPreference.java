package org.ostrya.presencepublisher.preference.connection;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.BooleanPreferenceBase;

public class RetainFlagPreference extends BooleanPreferenceBase {
    public static final String RETAIN_FLAG = "retainFlag";

    public RetainFlagPreference(Context context) {
        super(context, RETAIN_FLAG, R.string.retain_flag_title, R.string.retain_flag_summary);
    }
}
