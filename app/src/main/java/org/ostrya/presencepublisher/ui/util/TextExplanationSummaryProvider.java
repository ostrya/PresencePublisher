package org.ostrya.presencepublisher.ui.util;

import android.text.TextUtils;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import org.ostrya.presencepublisher.R;

public class TextExplanationSummaryProvider implements Preference.SummaryProvider<EditTextPreference> {
    private final int summaryId;

    public TextExplanationSummaryProvider(int summaryId) {
        this.summaryId = summaryId;
    }

    @Override
    public CharSequence provideSummary(EditTextPreference preference) {
        String value;
        if (TextUtils.isEmpty(preference.getText())) {
            value = (preference.getContext().getString(R.string.value_undefined));
        } else {
            value = preference.getText();
        }
        return String.format(preference.getContext().getString(summaryId), value);
    }
}
