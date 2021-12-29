package org.ostrya.presencepublisher.ui.preference.messages;

import android.content.Context;

import androidx.preference.ListPreference;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.message.MessageFormat;
import org.ostrya.presencepublisher.ui.util.MessageFormatSummaryProvider;

public class MessageFormatPreference extends ListPreference {
    public static final String MESSAGE_FORMAT_SETTING = "messageFormatSetting";

    public MessageFormatPreference(Context context) {
        super(context);
        setKey(MESSAGE_FORMAT_SETTING);
        setTitle(R.string.message_format_setting_title);
        setDialogTitle(R.string.message_format_setting_title);
        setSummaryProvider(new MessageFormatSummaryProvider());
        setIconSpaceReserved(false);
        setEntries(MessageFormat.settingDescriptions());
        setEntryValues(MessageFormat.settingValues());
        setDefaultValue(MessageFormat.PLAINTEXT.name());
    }
}
