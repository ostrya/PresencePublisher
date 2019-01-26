package org.ostrya.presencepublisher.mqtt;

import android.util.Log;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

class AndroidSslSocketFactoryFactory {
    private static final String TAG = AndroidSslSocketFactoryFactory.class.getSimpleName();

    static SSLSocketFactory getSslSocketFactory() {
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
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | CertificateException | IOException e) {
            Log.w(TAG, "Unable to get socket factory", e);
            return null;
        }
    }
}
