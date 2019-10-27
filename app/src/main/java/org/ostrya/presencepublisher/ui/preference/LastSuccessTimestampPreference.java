package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.ostrya.presencepublisher.R;

public class LastSuccessTimestampPreference extends AbstractTimestampPreference {
    public static final String LAST_SUCCESS = "lastPing";

    public LastSuccessTimestampPreference(Context context) {
        super(context, LAST_SUCCESS, R.string.last_success_title);
    }
}
