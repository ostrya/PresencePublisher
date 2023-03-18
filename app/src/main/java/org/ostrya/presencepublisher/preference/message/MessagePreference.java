package org.ostrya.presencepublisher.preference.message;

import static org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment.getInstance;
import static org.ostrya.presencepublisher.preference.message.MessageCategorySupport.MESSAGE_CONFIG_PREFIX;
import static org.ostrya.presencepublisher.preference.message.MessageCategorySupport.MESSAGE_LIST;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceViewHolder;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.dialog.EditMessageDialog;
import org.ostrya.presencepublisher.mqtt.message.MessageItem;
import org.ostrya.presencepublisher.preference.common.AbstractTextPreferenceEntry;
import org.ostrya.presencepublisher.preference.common.validation.MessageConfigurationValidator;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessagePreference extends AbstractTextPreferenceEntry
        implements View.OnLongClickListener {
    private final Fragment fragment;
    private final SharedPreferences preference;

    public MessagePreference(
            Context context,
            String key,
            String title,
            SharedPreferences preference,
            Fragment fragment) {
        super(context, key, new MessageConfigurationValidator(), title, R.string.message_summary);
        this.fragment = fragment;
        this.preference = preference;
    }

    @Override
    protected void onClick() {
        String name = getTitle().toString();
        String rawValue = getText();
        EditMessageDialog instance =
                EditMessageDialog.getInstance(
                        this::onEditText, name, MessageConfiguration.fromRawValue(rawValue));
        instance.show(fragment.getParentFragmentManager(), null);
    }

    private void onEditText(@Nullable String newName, @Nullable String newValue) {
        if (newName != null && !newName.equals(getTitle().toString())) {
            renameMessage(newName);
        }
        if (newValue != null && !newValue.equals(getText())) {
            if (callChangeListener(newValue)) {
                setText(newValue);
            }
        }
    }

    private void renameMessage(String newName) {
        Set<String> newNames =
                new HashSet<>(preference.getStringSet(MESSAGE_LIST, Collections.emptySet()));
        String oldName = getTitle().toString();
        newNames.remove(oldName);
        newNames.add(newName);
        String newKey = MESSAGE_CONFIG_PREFIX + newName;
        String value = getText();
        SharedPreferences.Editor editor = preference.edit();
        if (value != null) {
            editor.putString(newKey, value);
        }
        editor.remove(getKey()).putStringSet(MESSAGE_LIST, newNames).apply();
        setKey(newKey);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        ConfirmationDialogFragment instance =
                getInstance(
                        this::deleteOnContinue,
                        R.string.remove_message_title,
                        R.string.remove_message_warning_message);
        instance.show(fragment.getParentFragmentManager(), null);
        return true;
    }

    private void deleteOnContinue(Activity unused, boolean ok) {
        if (ok) {
            Set<String> newNames =
                    new HashSet<>(preference.getStringSet(MESSAGE_LIST, Collections.emptySet()));
            newNames.remove(getTitle().toString());
            preference.edit().remove(getKey()).putStringSet(MESSAGE_LIST, newNames).apply();
        }
    }

    @Override
    protected String getValue(@NonNull String text) {
        MessageConfiguration messageConfiguration = MessageConfiguration.fromRawValue(text);
        if (messageConfiguration != null) {
            return messageConfiguration.getTopic()
                    + "\n"
                    + getContent(messageConfiguration.getItems());
        } else {
            return getContext().getString(R.string.value_undefined);
        }
    }

    private String getContent(List<MessageItem> items) {
        StringBuilder sb = new StringBuilder();
        boolean needSeparator = false;
        for (MessageItem item : items) {
            if (needSeparator) {
                sb.append(", ");
            }
            sb.append(item.getName());
            needSeparator = true;
        }
        return getContext().getString(R.string.message_content, sb.toString());
    }
}
