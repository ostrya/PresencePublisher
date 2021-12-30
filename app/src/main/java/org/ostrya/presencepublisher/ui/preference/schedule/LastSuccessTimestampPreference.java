package org.ostrya.presencepublisher.ui.preference.schedule;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.TimestampPreferenceBase;

public class LastSuccessTimestampPreference extends TimestampPreferenceBase {
    public static final String LAST_SUCCESS = "lastPing";

    public LastSuccessTimestampPreference(Context context) {
        super(context, LAST_SUCCESS, R.string.last_success_title);
    }
}
