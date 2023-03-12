package org.ostrya.presencepublisher.ui.preference.connection;

import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.view.View;

import androidx.annotation.NonNull;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.security.SecurePreferencesHelper;
import org.ostrya.presencepublisher.ui.preference.common.TextPreferenceBase;

public class PasswordPreference extends TextPreferenceBase {
    public static final String PASSWORD = "password";

    public PasswordPreference(Context context) {
        super(
                context,
                PASSWORD,
                (c, k, v) -> true,
                R.string.password_title,
                R.string.password_summary);
        setOnBindEditTextListener(
                editText -> {
                    editText.setInputType(
                            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editText.setSelection(editText.getText().length());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        editText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD);
                    }
                });
        setPreferenceDataStore(SecurePreferencesHelper.getSecurePreferenceDataStore(context));
    }

    @Override
    protected String getValue(@NonNull String ignored) {
        return getContext().getString(R.string.password_placeholder);
    }
}
