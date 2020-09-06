package org.ostrya.presencepublisher.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDataStore;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.hypertrack.hyperlog.HyperLog;

public class SecurePreferencesHelper {
    private static final String TAG = "SecurePreferencesHelper";

    private static final String FILENAME = "encryptedPreferences";

    public static PreferenceDataStore getSecurePreferenceDataStore(Context context) {
        return new PreferenceDataStore() {
            @Override
            public void putString(String key, @Nullable String value) {
                SharedPreferences preferences = getSecurePreferences(context);
                preferences.edit().putString(key, value).apply();
            }

            @Nullable
            @Override
            public String getString(String key, @Nullable String defValue) {
                SharedPreferences preferences = getSecurePreferences(context);
                return preferences.getString(key, defValue);
            }
        };
    }

    public static SharedPreferences getSecurePreferences(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                MasterKey masterKey = new MasterKey.Builder(context)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build();
                return EncryptedSharedPreferences
                        .create(
                                context,
                                FILENAME,
                                masterKey,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        );
            } catch (Exception e) {
                HyperLog.w(TAG, "Unable to get secure preferences", e);
                throw new RuntimeException("Unable to get secure preferences");
            }
        } else {
            return context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        }
    }
}
