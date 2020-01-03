package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;
import androidx.preference.SwitchPreferenceCompat;

public class BooleanPreferenceBase extends SwitchPreferenceCompat {
    public BooleanPreferenceBase(Context context, String key, int titleId, int summaryId) {
        super(context);
        setKey(key);
        setIconSpaceReserved(false);
        setSummary(summaryId);
        setTitle(titleId);
    }
}
