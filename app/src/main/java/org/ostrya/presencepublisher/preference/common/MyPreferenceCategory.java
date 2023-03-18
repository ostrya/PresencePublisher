package org.ostrya.presencepublisher.preference.common;

import android.content.Context;

import androidx.preference.PreferenceCategory;

public class MyPreferenceCategory extends PreferenceCategory {
    public MyPreferenceCategory(Context context, int titleId) {
        super(context);
        setTitle(titleId);
        setIconSpaceReserved(false);
    }
}
