package org.ostrya.presencepublisher.ui.util;

import android.content.Context;
import android.widget.Toast;
import androidx.preference.EditTextPreference;
import org.ostrya.presencepublisher.R;

import static org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider.PreferenceType.STRING;

public class EditTextPreferencesHelper {

    public static EditTextPreference getEditTextPreference(final Context context, final String key, final int titleId,
                                                           final int summaryId, final Validator validator) {
        EditTextPreference preference = new EditTextPreference(context);
        preference.setKey(key);
        preference.setTitle(context.getString(titleId));
        preference.setSummaryProvider(new ExplanationSummaryProvider(summaryId, STRING));
        preference.setIconSpaceReserved(false);
        preference.setOnPreferenceChangeListener(((prefs, newValue) -> {
            boolean result = validator.isValid((String) newValue);
            if (!result) {
                String text = context.getString(R.string.toast_invalid_input);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
            return result;
        }));
        return preference;
    }

    public static EditTextPreference getEditTextPreference(final Context context, final String key, final String title,
                                                           final int summaryId, final String defaultValue,
                                                           final Validator validator) {
        EditTextPreference preference = new EditTextPreference(context);
        preference.setKey(key);
        preference.setTitle(title);
        preference.setSummaryProvider(new ExplanationSummaryProvider(summaryId, STRING));
        preference.setIconSpaceReserved(false);
        preference.setOnPreferenceChangeListener(((prefs, newValue) -> {
            boolean result = validator.isValid((String) newValue);
            if (!result) {
                String text = context.getString(R.string.toast_invalid_input);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
            return result;
        }));
        preference.setDefaultValue(defaultValue);
        return preference;
    }

}
