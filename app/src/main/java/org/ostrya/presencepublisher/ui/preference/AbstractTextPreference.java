package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import android.widget.Toast;
import androidx.preference.EditTextPreference;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;
import org.ostrya.presencepublisher.ui.util.Validator;

import static org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider.PreferenceType.STRING;

class AbstractTextPreference extends EditTextPreference {
    private final Validator validator;

    AbstractTextPreference(Context context, String key, Validator validator, int titleId, int summaryId) {
        this(context, key, validator, summaryId);
        setTitle(titleId);
        setDialogTitle(titleId);
    }

    AbstractTextPreference(Context context, String key, Validator validator, String title, int summaryId) {
        this(context, key, validator, summaryId);
        setTitle(title);
        setDialogTitle(title);
    }

    private AbstractTextPreference(Context context, String key, Validator validator, int summaryId) {
        super(context);
        setKey(key);
        setIconSpaceReserved(false);
        this.validator = validator;
        setOnPreferenceChangeListener(((prefs, newValue) -> {
            boolean result = this.validator.isValid((String) newValue);
            if (!result) {
                String text = context.getString(R.string.toast_invalid_input);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
            return result;
        }));
        configureSummary(summaryId);
    }

    protected void configureSummary(int summaryId) {
        setSummaryProvider(new ExplanationSummaryProvider(summaryId, STRING));
    }
}
