package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;
import androidx.preference.Preference;

public class ContentHelpDummy extends Preference {
    public ContentHelpDummy(Context context, int explanationId) {
        super(context);
        setSummary(explanationId);
        setIconSpaceReserved(false);
    }
}
