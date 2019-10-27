package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import androidx.preference.Preference;
import org.ostrya.presencepublisher.ui.util.TimestampSummaryProvider;

class AbstractTimestampPreference extends Preference {
    AbstractTimestampPreference(Context context, String key, int titleId) {
        super(context);
        setKey(key);
        setTitle(titleId);
        setSummaryProvider(new TimestampSummaryProvider());
        setIconSpaceReserved(false);
    }

    public void refresh() {
        boolean copyingEnabled = isCopyingEnabled();
        setCopyingEnabled(!copyingEnabled);
        setCopyingEnabled(copyingEnabled);
    }
}
