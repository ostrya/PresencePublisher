package org.ostrya.presencepublisher.ui.preference.messages;

import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_CONFIG_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_LIST;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.EditMessageDialog;
import org.ostrya.presencepublisher.ui.preference.common.ClickDummy;
import org.ostrya.presencepublisher.ui.util.MessageConfigurationValidator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AddMessageChoicePreferenceDummy extends ClickDummy {
    private final SharedPreferences preference;
    private final MessageConfigurationValidator validator;

    public AddMessageChoicePreferenceDummy(
            Context context, SharedPreferences preference, Fragment fragment) {
        super(
                context,
                R.drawable.baseline_playlist_add_24,
                R.string.add_message_title,
                R.string.add_message_summary,
                fragment);
        this.preference = preference;
        this.validator = new MessageConfigurationValidator();
    }

    @Override
    protected void onClick() {
        EditMessageDialog instance = EditMessageDialog.getInstance(this::onEditText, null, null);
        instance.show(getParentFragmentManager(), null);
    }

    private void onEditText(@Nullable String name, @Nullable String value) {
        if (!validator.isValid(
                getContext(), name == null ? null : MESSAGE_CONFIG_PREFIX + name, value)) {
            return;
        }
        Set<String> newNames =
                new HashSet<>(preference.getStringSet(MESSAGE_LIST, Collections.emptySet()));
        newNames.add(name);
        SharedPreferences.Editor editor = preference.edit();
        editor.putStringSet(MESSAGE_LIST, newNames)
                .putString(MESSAGE_CONFIG_PREFIX + name, value)
                .apply();
    }
}
