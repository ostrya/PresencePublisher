package org.ostrya.presencepublisher.preference.common;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.validation.Validator;

public abstract class TextPreferenceBase extends EditTextPreference
        implements Preference.SummaryProvider<TextPreferenceBase> {
    private final Validator validator;
    private final int summaryId;

    public TextPreferenceBase(
            Context context, String key, Validator validator, int titleId, int summaryId) {
        this(context, key, validator, summaryId);
        setTitle(titleId);
        setDialogTitle(titleId);
    }

    public TextPreferenceBase(
            Context context, String key, Validator validator, CharSequence title, int summaryId) {
        this(context, key, validator, summaryId);
        setTitle(title);
        setDialogTitle(title);
    }

    private TextPreferenceBase(Context context, String key, Validator validator, int summaryId) {
        super(context);
        setKey(key);
        setIconSpaceReserved(false);
        this.validator = validator;
        this.summaryId = summaryId;
        setOnPreferenceChangeListener(
                ((prefs, newValue) -> {
                    boolean result = this.validator.isValid(context, getKey(), (String) newValue);
                    if (result && ((String) newValue).isEmpty()) {
                        setText(null);
                        return false;
                    }
                    return result;
                }));
        setSummaryProvider(this);
    }

    @Nullable
    @Override
    public CharSequence provideSummary(@NonNull TextPreferenceBase preference) {
        String contentValue = preference.getContentValue();
        Context context = preference.getContext();
        if (TextUtils.isEmpty(contentValue)) {
            return context.getString(summaryId, context.getString(R.string.value_undefined));
        } else {
            return context.getString(summaryId, contentValue);
        }
    }

    protected String getContentValue() {
        return getText();
    }
}
