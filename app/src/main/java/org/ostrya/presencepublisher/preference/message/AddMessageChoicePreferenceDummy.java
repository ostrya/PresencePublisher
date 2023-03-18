package org.ostrya.presencepublisher.preference.message;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.EditMessageDialog;
import org.ostrya.presencepublisher.preference.common.ClickDummy;
import org.ostrya.presencepublisher.preference.common.validation.MessageConfigurationValidator;

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
                getContext(),
                name == null ? null : MessageCategorySupport.MESSAGE_CONFIG_PREFIX + name,
                value)) {
            return;
        }
        Set<String> newNames =
                new HashSet<>(
                        preference.getStringSet(
                                MessageCategorySupport.MESSAGE_LIST, Collections.emptySet()));
        newNames.add(name);
        SharedPreferences.Editor editor = preference.edit();
        editor.putStringSet(MessageCategorySupport.MESSAGE_LIST, newNames)
                .putString(MessageCategorySupport.MESSAGE_CONFIG_PREFIX + name, value)
                .apply();
    }
}
