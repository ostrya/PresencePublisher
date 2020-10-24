package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;
import android.widget.Toast;
import androidx.preference.EditTextPreference;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.Validator;

public abstract class TextPreferenceBase extends EditTextPreference {
    private final Validator validator;

    public TextPreferenceBase(Context context, String key, Validator validator, int titleId) {
        this(context, key, validator);
        setTitle(titleId);
        setDialogTitle(titleId);
    }

    public TextPreferenceBase(Context context, String key, Validator validator, CharSequence title) {
        this(context, key, validator);
        setTitle(title);
        setDialogTitle(title);
    }

    private TextPreferenceBase(Context context, String key, Validator validator) {
        super(context);
        setKey(key);
        setIconSpaceReserved(false);
        this.validator = validator;
        setOnPreferenceChangeListener(((prefs, newValue) -> {
            boolean result = this.validator.isValid((String) newValue);
            if (!result) {
                String text = context.getString(R.string.toast_invalid_input);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            } else if (((String) newValue).isEmpty()) {
                setText(null);
                return false;
            }
            return result;
        }));
        configureSummary();
    }

    protected abstract void configureSummary();
}
