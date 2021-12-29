package org.ostrya.presencepublisher.ui.util;

import androidx.preference.Preference;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.message.MessageItem;
import org.ostrya.presencepublisher.ui.preference.messages.MessageConfiguration;
import org.ostrya.presencepublisher.ui.preference.messages.MessagePreference;

public class MessageSummaryProvider implements Preference.SummaryProvider<MessagePreference> {
    @Override
    public CharSequence provideSummary(MessagePreference preference) {
        String undefined = preference.getContext().getString(R.string.value_undefined);
        MessageConfiguration messageConfiguration =
                MessageConfiguration.fromRawValue(
                        preference
                                .getSharedPreferences()
                                .getString(preference.getKey(), undefined));
        if (messageConfiguration != null) {
            if (messageConfiguration.getItems().size() == 1) {
                return preference
                        .getContext()
                        .getString(
                                R.string.message_summary,
                                messageConfiguration.getTopic(),
                                messageConfiguration.getItems().get(0).getName());
            } else {
                StringBuilder sb = new StringBuilder();
                boolean continuation = false;
                for (MessageItem item : messageConfiguration.getItems()) {
                    if (continuation) {
                        sb.append(", ");
                    }
                    continuation = true;
                    sb.append(item.getName());
                }
                return preference
                        .getContext()
                        .getString(
                                R.string.message_summary,
                                messageConfiguration.getTopic(),
                                sb.toString());
            }
        } else {
            return preference
                    .getContext()
                    .getString(R.string.message_summary, undefined, undefined);
        }
    }
}
