package org.ostrya.presencepublisher.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.AbstractConfigurationFragment;
import org.ostrya.presencepublisher.preference.common.MyPreferenceCategory;
import org.ostrya.presencepublisher.preference.common.StringDummy;
import org.ostrya.presencepublisher.preference.message.MessageCategorySupport;
import org.ostrya.presencepublisher.preference.message.MessageFormatHelpDummy;
import org.ostrya.presencepublisher.preference.message.MessageFormatPreference;

public class MessageFragment extends AbstractConfigurationFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference messageHelpSummary = new StringDummy(context, R.string.message_help_summary);
        PreferenceCategory commonCategory =
                new MyPreferenceCategory(context, R.string.category_common_settings);
        MessageCategorySupport messageCategorySupport = new MessageCategorySupport(this);
        PreferenceCategory messageCategory = messageCategorySupport.getCategory();

        screen.addPreference(messageHelpSummary);
        screen.addPreference(commonCategory);
        screen.addPreference(messageCategory);

        Preference conditionPlaceholderSetting = new MessageFormatPreference(context);
        Preference messageFormatHelpSummary = new MessageFormatHelpDummy(context, this);

        commonCategory.addPreference(conditionPlaceholderSetting);
        commonCategory.addPreference(messageFormatHelpSummary);

        messageCategorySupport.initializeCategory();

        setPreferenceScreen(screen);
    }

    @Override
    protected void onPreferencesChanged(SharedPreferences preferences, String name) {
        // empty
    }
}
