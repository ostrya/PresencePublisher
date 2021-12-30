package org.ostrya.presencepublisher.ui.preference.about;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.StringDummy;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SignaturePreferenceDummy extends StringDummy {
    private static final String TAG = "SignaturePreferenceDummy";

    public SignaturePreferenceDummy(Context context) {
        super(context, R.string.signature_title, getSignatures(context));
    }

    private static String getSignatures(Context context) {
        try {
            Signature[] signatures;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo info =
                        context.getPackageManager()
                                .getPackageInfo(
                                        context.getPackageName(),
                                        PackageManager.GET_SIGNING_CERTIFICATES);
                if (info.signingInfo == null) {
                    signatures = null;
                } else if (info.signingInfo.hasMultipleSigners()) {
                    signatures = info.signingInfo.getApkContentsSigners();
                } else {
                    signatures = info.signingInfo.getSigningCertificateHistory();
                }
            } else {
                signatures = getLegacySignatures(context);
            }
            if (signatures == null) {
                HyperLog.i(TAG, "No signing info found");
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
            HyperLog.w(TAG, "Unable to find this package", e);
            return context.getString(R.string.value_undefined);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("PackageManagerGetSignatures")
    private static Signature[] getLegacySignatures(Context context)
            throws PackageManager.NameNotFoundException {
        PackageInfo info =
                context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
        return info.signatures;
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
            HyperLog.w(TAG, "Unable to instantiate X.509 certificate factory", e);
            return defaultValue;
        }
    }
}
