package org.ostrya.presencepublisher.preference.connection;

import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.device.DevicePreferences;
import org.ostrya.presencepublisher.preference.common.TextPreferenceBase;

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
        setPreferenceDataStore(new DevicePreferences(context));
    }

    @Override
    protected String getContentValue() {
        if (TextUtils.isEmpty(getText())) {
            return null;
        }
        return getContext().getString(R.string.password_placeholder);
    }
}
