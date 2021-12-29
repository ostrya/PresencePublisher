package org.ostrya.presencepublisher.ui.util;

import androidx.preference.Preference;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.messages.MessageFormatPreference;

public class MessageFormatSummaryProvider
        implements Preference.SummaryProvider<MessageFormatPreference> {
    @Override
    public CharSequence provideSummary(MessageFormatPreference preference) {
        return preference
                .getContext()
                .getString(R.string.message_format_setting_summary, getValue(preference));
    }

    private String getValue(MessageFormatPreference preference) {
        String undefined = preference.getContext().getString(R.string.value_undefined);
        if (preference.hasKey()) {
            return preference.getEntry().toString();
        } else {
            return undefined;
        }
    }
}
