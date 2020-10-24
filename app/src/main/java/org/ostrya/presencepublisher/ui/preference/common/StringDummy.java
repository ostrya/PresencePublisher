package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;
import androidx.preference.Preference;

public class StringDummy extends Preference {
    public StringDummy(Context context, int summaryId) {
        super(context);
        setSummary(summaryId);
        setIconSpaceReserved(false);
    }

    public StringDummy(Context context, int titleId, int summaryId) {
        super(context);
        setTitle(titleId);
        setSummary(summaryId);
        setIconSpaceReserved(false);
    }

    public StringDummy(Context context, int titleId, CharSequence summary) {
        super(context);
        setTitle(titleId);
        setSummary(summary);
        setIconSpaceReserved(false);
    }
}
