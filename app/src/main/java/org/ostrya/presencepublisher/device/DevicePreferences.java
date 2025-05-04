package org.ostrya.presencepublisher.device;

import static android.security.keystore.KeyProperties.BLOCK_MODE_GCM;
import static android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE;
import static android.security.keystore.KeyProperties.KEY_ALGORITHM_AES;
import static android.security.keystore.KeyProperties.PURPOSE_DECRYPT;
import static android.security.keystore.KeyProperties.PURPOSE_ENCRYPT;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import androidx.preference.PreferenceDataStore;

import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Helper class to store data which is device-specific
 *
 * <p>For extra safety, this is encrypted starting with Android 6. No encryption in older versions,
 * because Google decided to delete the required class KeyGeneratorSpec from the more recent SDKs.
 */
public class DevicePreferences extends PreferenceDataStore {
    private static final String TAG = "DevicePreferences";
    private static final String NAME = "devicePreferences";
    private static final String ALIAS = "PresencePublisherKey";

    private final SharedPreferences devicePreferences;

    public DevicePreferences(Context context) {
        this.devicePreferences =
                context.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    @VisibleForTesting
    DevicePreferences(SharedPreferences devicePreferences) {
        this.devicePreferences = devicePreferences;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private SecretKey getOrInitKey()
            throws KeyStoreException,
                    UnrecoverableKeyException,
                    NoSuchAlgorithmException,
                    NoSuchProviderException,
                    CertificateException,
                    IOException,
                    InvalidAlgorithmParameterException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (keyStore.isKeyEntry(ALIAS)) {
            return (SecretKey) keyStore.getKey(ALIAS, null);
        }
        KeyGenerator generator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
        generator.init(
                new KeyGenParameterSpec.Builder(ALIAS, PURPOSE_ENCRYPT | PURPOSE_DECRYPT)
                        .setKeySize(256)
                        .setBlockModes(BLOCK_MODE_GCM)
                        .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                        .build());
        SecretKey key = generator.generateKey();
        keyStore.setKeyEntry(ALIAS, key, null, null);
        return key;
    }

    @Override
    public void putString(String key, @Nullable String value) {
        if (value != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    SecretKey secretKey = getOrInitKey();
                    Cipher cipher =
                            Cipher.getInstance(
                                    KEY_ALGORITHM_AES
                                            + "/"
                                            + BLOCK_MODE_GCM
                                            + "/"
                                            + ENCRYPTION_PADDING_NONE);
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                    byte[] iv = cipher.getIV();
                    int tagLength =
                            cipher.getParameters()
                                    .getParameterSpec(GCMParameterSpec.class)
                                    .getTLen();
                    byte[] cipherText = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
                    String encrypted =
                            Base64.getEncoder().encodeToString(iv)
                                    + "!"
                                    + tagLength
                                    + "!"
                                    + Base64.getEncoder().encodeToString(cipherText);
                    devicePreferences.edit().putString(key, encrypted).apply();
                } catch (Exception e) {
                    DatabaseLogger.e(TAG, "Unable to encrypt value for '" + key + "'", e);
                }
            } else {
                devicePreferences.edit().putString(key, value).apply();
            }
        } else {
            devicePreferences.edit().remove(key).apply();
        }
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String value = devicePreferences.getString(key, null);
                if (value == null) {
                    return defValue;
                }
                String[] split = value.split("!", 3);
                if (split.length != 3) {
                    DatabaseLogger.w(TAG, "Ignoring malformed value for key " + key);
                    return defValue;
                }
                byte[] iv = Base64.getDecoder().decode(split[0]);
                int tagLength = Integer.parseInt(split[1]);
                byte[] encrypted = Base64.getDecoder().decode(split[2]);
                SecretKey secretKey = getOrInitKey();
                Cipher cipher =
                        Cipher.getInstance(
                                KEY_ALGORITHM_AES
                                        + "/"
                                        + BLOCK_MODE_GCM
                                        + "/"
                                        + ENCRYPTION_PADDING_NONE);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(tagLength, iv));
                return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
            } catch (Exception e) {
                DatabaseLogger.w(TAG, "Unable to decrypt value for key " + key, e);
                return defValue;
            }
        } else {
            return devicePreferences.getString(key, defValue);
        }
    }

    public boolean contains(String key) {
        return devicePreferences.contains(key);
    }
}
