package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;
import androidx.preference.Preference;
import org.ostrya.presencepublisher.ui.util.TimestampSummaryProvider;

public class TimestampPreferenceBase extends Preference {
    public TimestampPreferenceBase(Context context, String key, int titleId) {
        super(context);
        setKey(key);
        setTitle(titleId);
        setSummaryProvider(new TimestampSummaryProvider<>());
        setIconSpaceReserved(false);
    }

    public void refresh() {
        boolean copyingEnabled = isCopyingEnabled();
        setCopyingEnabled(!copyingEnabled);
        setCopyingEnabled(copyingEnabled);
    }
}
