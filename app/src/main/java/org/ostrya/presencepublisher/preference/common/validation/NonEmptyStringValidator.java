package org.ostrya.presencepublisher.preference.common.validation;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.ostrya.presencepublisher.R;

public class NonEmptyStringValidator implements Validator {
    @Override
    public boolean isValid(Context context, @Nullable String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            return true;
        } else {
            String text = context.getString(R.string.toast_invalid_input);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
