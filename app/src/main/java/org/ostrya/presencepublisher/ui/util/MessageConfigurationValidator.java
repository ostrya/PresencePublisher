package org.ostrya.presencepublisher.ui.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.messages.MessageConfiguration;

public class MessageConfigurationValidator implements Validator {

    @Override
    public boolean isValid(Context context, @Nullable String key, String value) {
        if (key == null) {
            String text = context.getString(R.string.toast_empty_name);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            return false;
        }
        MessageConfiguration messageConfiguration = MessageConfiguration.fromRawValue(value);
        if (messageConfiguration == null) {
            String text = context.getString(R.string.toast_invalid_input);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (messageConfiguration.getTopic().isEmpty()) {
            String text = context.getString(R.string.toast_empty_topic);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (messageConfiguration.getItems().isEmpty()) {
            String text = context.getString(R.string.toast_empty_name);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
