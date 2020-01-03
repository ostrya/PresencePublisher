package org.ostrya.presencepublisher.ui.util;

import androidx.preference.Preference;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.connection.PasswordPreference;

public class PasswordSummaryProvider implements Preference.SummaryProvider<PasswordPreference> {
    private final int summaryId;

    public PasswordSummaryProvider(int summaryId) {
        this.summaryId = summaryId;
    }

    @Override
    public CharSequence provideSummary(PasswordPreference preference) {
        return preference.getContext().getString(summaryId, getValue(preference));
    }

    private String getValue(PasswordPreference preference) {
        if (preference.getText() != null) {
            return preference.getContext().getString(R.string.password_placeholder);
        } else {
            return preference.getContext().getString(R.string.value_undefined);
        }
    }
}
