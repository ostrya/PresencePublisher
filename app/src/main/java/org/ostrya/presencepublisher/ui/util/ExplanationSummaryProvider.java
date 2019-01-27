package org.ostrya.presencepublisher.ui.util;

import androidx.preference.Preference;
import org.ostrya.presencepublisher.R;

public class ExplanationSummaryProvider<T extends Preference> implements Preference.SummaryProvider<T> {
    private final int summaryId;

    public ExplanationSummaryProvider(int summaryId) {
        this.summaryId = summaryId;
    }

    @Override
    public CharSequence provideSummary(T preference) {
        return String.format(preference.getContext().getString(summaryId), getValue(preference));
    }

    private String getValue(T preference) {
        String undefined = preference.getContext().getString(R.string.value_undefined);
        if (preference.hasKey()) {
            return preference.getSharedPreferences().getString(preference.getKey(), undefined);
        } else {
            return undefined;
        }
    }
}
