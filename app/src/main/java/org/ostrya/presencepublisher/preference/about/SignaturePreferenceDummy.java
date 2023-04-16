package org.ostrya.presencepublisher.preference.about;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import androidx.core.content.pm.PackageInfoCompat;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.preference.common.StringDummy;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

public class SignaturePreferenceDummy extends StringDummy {
    private static final String TAG = "SignaturePreferenceDummy";

    public SignaturePreferenceDummy(Context context) {
        super(context, R.string.signature_title, getSignatures(context));
    }

    private static String getSignatures(Context context) {
        try {
            List<Signature> signatures =
                    PackageInfoCompat.getSignatures(
                            context.getPackageManager(), context.getPackageName());
            if (signatures.isEmpty()) {
                DatabaseLogger.i(TAG, "No signing info found");
                return context.getString(R.string.value_undefined);
            }
            boolean moreThanOne = false;
            StringBuilder sb = new StringBuilder();
            for (Signature s : signatures) {
                if (moreThanOne) {
                    sb.append("\n");
                } else {
                    moreThanOne = true;
                }
                sb.append(signatureToString(s, context.getString(R.string.value_undefined)));
            }
            return sb.toString();
        } catch (PackageManager.NameNotFoundException e) {
            DatabaseLogger.w(TAG, "Unable to find this package", e);
            return context.getString(R.string.value_undefined);
        }
    }

    private static String signatureToString(Signature signature, String defaultValue) {
        try {
            X509Certificate cert =
                    (X509Certificate)
                            CertificateFactory.getInstance("X.509")
                                    .generateCertificate(
                                            new ByteArrayInputStream(signature.toByteArray()));
            return cert.toString();
        } catch (CertificateException e) {
            DatabaseLogger.w(TAG, "Unable to instantiate X.509 certificate factory", e);
            return defaultValue;
        }
    }
}
