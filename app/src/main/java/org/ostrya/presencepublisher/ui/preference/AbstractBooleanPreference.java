package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import androidx.preference.SwitchPreferenceCompat;

class AbstractBooleanPreference extends SwitchPreferenceCompat {
    AbstractBooleanPreference(Context context, String key, int titleId, int summaryId) {
        super(context);
        setKey(key);
        setIconSpaceReserved(false);
        setSummary(summaryId);
        setTitle(titleId);
    }
}
