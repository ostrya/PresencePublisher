package org.ostrya.presencepublisher.ui.util;

import android.text.TextUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import org.ostrya.presencepublisher.R;

public class ListExplanationSummaryProvider implements Preference.SummaryProvider<ListPreference> {
    private final int summaryId;

    public ListExplanationSummaryProvider(int summaryId) {
        this.summaryId = summaryId;
    }

    @Override
    public CharSequence provideSummary(ListPreference preference) {
        String value;
        if (TextUtils.isEmpty(preference.getValue())) {
            value = (preference.getContext().getString(R.string.value_undefined));
        } else {
            value = preference.getValue();
        }
        return String.format(preference.getContext().getString(summaryId), value);
    }
}
