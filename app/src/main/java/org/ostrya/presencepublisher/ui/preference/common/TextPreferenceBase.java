package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;

import androidx.preference.EditTextPreference;

import org.ostrya.presencepublisher.ui.util.Validator;

public abstract class TextPreferenceBase extends EditTextPreference {
    private final Validator validator;

    public TextPreferenceBase(Context context, String key, Validator validator, int titleId) {
        this(context, key, validator);
        setTitle(titleId);
        setDialogTitle(titleId);
    }

    public TextPreferenceBase(
            Context context, String key, Validator validator, CharSequence title) {
        this(context, key, validator);
        setTitle(title);
        setDialogTitle(title);
    }

    private TextPreferenceBase(Context context, String key, Validator validator) {
        super(context);
        setKey(key);
        setIconSpaceReserved(false);
        this.validator = validator;
        setOnPreferenceChangeListener(
                ((prefs, newValue) -> {
                    boolean result = this.validator.isValid(context, getKey(), (String) newValue);
                    if (result && ((String) newValue).isEmpty()) {
                        setText(null);
                        return false;
                    }
                    return result;
                }));
        configureSummary();
    }

    protected abstract void configureSummary();
}
