package org.ostrya.presencepublisher.mqtt;

import android.content.Context;
import android.security.KeyChain;

import androidx.annotation.Nullable;

import com.hypertrack.hyperlog.HyperLog;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

class AndroidSslSocketFactoryFactory {
    private static final String TAG = "AndroidSslSocketFactoryFactory";

    private final Context context;

    AndroidSslSocketFactoryFactory(Context context) {
        this.context = context;
    }

    SSLSocketFactory getSslSocketFactory(@Nullable String clientCertAlias) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore androidCAStore = KeyStore.getInstance("AndroidCAStore");
            if (androidCAStore == null) {
                HyperLog.w(TAG, "Unable to load CA keystore");
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
        } catch (NoSuchAlgorithmException
                | KeyStoreException
                | KeyManagementException
                | CertificateException
                | IOException e) {
            HyperLog.w(TAG, "Unable to get socket factory", e);
            return null;
        }
    }

    @Nullable
    private KeyManager[] getClientKeyManagers(String clientCertAlias) {
        try {
            PrivateKey privateKey = KeyChain.getPrivateKey(context, clientCertAlias);
            X509Certificate[] certificateChain =
                    KeyChain.getCertificateChain(context, clientCertAlias);
            KeyStore customKeyStore = KeyStore.getInstance("PKCS12");
            char[] pwdArray = Double.toString(Math.random()).toCharArray();
            customKeyStore.load(null, pwdArray);
            customKeyStore.setKeyEntry(clientCertAlias, privateKey, null, certificateChain);
            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(customKeyStore, pwdArray);
            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            HyperLog.w(TAG, "Unable to initialize client key store", e);
            return null;
        }
    }
}
