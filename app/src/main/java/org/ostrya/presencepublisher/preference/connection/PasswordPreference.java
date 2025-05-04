package org.ostrya.presencepublisher.preference.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Supplier;
import androidx.preference.PreferenceDataStore;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
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
        setPreferenceDataStore(new SecureDataStore(context));
    }

    @Override
    protected String getContentValue() {
        if (TextUtils.isEmpty(getText())) {
            return null;
        }
        return getContext().getString(R.string.password_placeholder);
    }

    @Deprecated
    public static Supplier<String> getOldPasswordProvider(@NonNull Context context) {
        SecureDataStore dataStore = new SecureDataStore(context);
        return () -> dataStore.getString(PASSWORD, "");
    }

    @Deprecated
    private static class SecureDataStore extends PreferenceDataStore {
        private static final String TAG = "SecureDataStore";

        private static final String FILENAME = "encryptedPreferences";

        private final SharedPreferences preferences;

        public SecureDataStore(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    MasterKey masterKey =
                            new MasterKey.Builder(context)
                                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                                    .build();
                    preferences =
                            EncryptedSharedPreferences.create(
                                    context,
                                    FILENAME,
                                    masterKey,
                                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                    EncryptedSharedPreferences.PrefValueEncryptionScheme
                                            .AES256_GCM);
                } catch (Exception e) {
                    DatabaseLogger.w(TAG, "Unable to get secure preferences", e);
                    throw new RuntimeException("Unable to get secure preferences");
                }
            } else {
                preferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
            }
        }

        @Override
        public void putString(String key, @Nullable String value) {
            preferences.edit().putString(key, value).apply();
        }

        @Nullable
        @Override
        public String getString(String key, @Nullable String defValue) {
            return preferences.getString(key, defValue);
        }
    }
}
