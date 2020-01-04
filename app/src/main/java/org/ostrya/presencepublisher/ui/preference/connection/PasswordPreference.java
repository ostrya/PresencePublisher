package org.ostrya.presencepublisher.ui.preference.connection;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.security.SecurePreferencesHelper;
import org.ostrya.presencepublisher.ui.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.ui.util.PasswordSummaryProvider;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

public class PasswordPreference extends TextPreferenceBase {
    public static final String PASSWORD = "password";

    public PasswordPreference(Context context) {
        super(context, PASSWORD, new RegexValidator(".*"), R.string.password_title);
        setOnBindEditTextListener(editText -> {
            editText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setSelection(editText.getText().length());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                editText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD);
            }
        });
        setPreferenceDataStore(SecurePreferencesHelper.getSecurePreferenceDataStore(context));
    }

    @Override
    protected void configureSummary() {
        setSummaryProvider(new PasswordSummaryProvider(R.string.password_summary));
    }
}
