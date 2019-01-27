package org.ostrya.presencepublisher.mqtt;

import android.content.Context;
import android.security.KeyChain;
import android.util.Log;
import androidx.annotation.Nullable;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

class AndroidSslSocketFactoryFactory {
    private static final String TAG = AndroidSslSocketFactoryFactory.class.getSimpleName();

    private final Context context;

    AndroidSslSocketFactoryFactory(Context context) {
        this.context = context;
    }

    SSLSocketFactory getSslSocketFactory(@Nullable String clientCertAlias) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore androidCAStore = KeyStore.getInstance("AndroidCAStore");
            if (androidCAStore == null) {
                Log.w(TAG, "Unable to load CA keystore");
                return null;
            }
            androidCAStore.load(null);
            trustManagerFactory.init(androidCAStore);
            KeyManager[] keyManagers = null;
            if (clientCertAlias != null) {
                keyManagers = getClientKeyManagers(clientCertAlias);
            }
            sslContext.init(keyManagers, trustManagerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | CertificateException | IOException e) {
            Log.w(TAG, "Unable to get socket factory", e);
            return null;
        }
    }

    private KeyManager[] getClientKeyManagers(String clientCertAlias) {
        try {
            PrivateKey privateKey = KeyChain.getPrivateKey(context, clientCertAlias);
            X509Certificate[] certificateChain = KeyChain.getCertificateChain(context, clientCertAlias);
            KeyStore customKeyStore = KeyStore.getInstance("PKCS12");
            char[] pwdArray = Double.toString(Math.random()).toCharArray();
            customKeyStore.load(null, pwdArray);
            customKeyStore.setKeyEntry(clientCertAlias, privateKey, null, certificateChain);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(customKeyStore, pwdArray);
            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            Log.w(TAG, "Unable to initialize client key store", e);
            return null;
        }
    }
}
