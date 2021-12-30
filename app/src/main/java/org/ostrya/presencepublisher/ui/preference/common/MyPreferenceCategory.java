package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;

import androidx.preference.PreferenceCategory;

public class MyPreferenceCategory extends PreferenceCategory {
    public MyPreferenceCategory(Context context, int titleId) {
        super(context);
        setTitle(titleId);
        setIconSpaceReserved(false);
    }
}
