package org.ostrya.presencepublisher.preference.common.validation;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.ostrya.presencepublisher.R;

public class RangeValidator implements Validator {
    private final long min;
    private final long max;

    public RangeValidator(long min, long max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isValid(Context context, @Nullable String key, String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        long parsed;
        try {
            parsed = Long.parseLong(value);
        } catch (NumberFormatException e) {
            String text = context.getString(R.string.toast_invalid_number_input);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (parsed >= min && parsed <= max) {
            return true;
        }
        String text = context.getString(R.string.toast_invalid_number_range, min, max);
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        return false;
    }
}
