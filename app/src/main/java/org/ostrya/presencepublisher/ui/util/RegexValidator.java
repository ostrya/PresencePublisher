package org.ostrya.presencepublisher.ui.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.ostrya.presencepublisher.R;

import java.util.regex.Pattern;

public class RegexValidator implements Validator {
    private final Pattern pattern;

    public RegexValidator(final String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean isValid(Context context, @Nullable String key, String value) {
        if (pattern.matcher(value).matches()) {
            return true;
        } else {
            String text = context.getString(R.string.toast_invalid_input);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
