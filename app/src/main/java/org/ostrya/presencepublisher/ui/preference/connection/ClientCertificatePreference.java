package org.ostrya.presencepublisher.ui.preference.connection;

import static org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider.PreferenceType.STRING;

import android.content.Context;
import android.security.KeyChain;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;

public class ClientCertificatePreference extends Preference {
    public static final String CLIENT_CERTIFICATE = "client_cert";

    public ClientCertificatePreference(Context context, Fragment fragment) {
        super(context);
        setKey(CLIENT_CERTIFICATE);
        setTitle(R.string.client_certificate_title);
        setSummaryProvider(
                new ExplanationSummaryProvider<>(R.string.client_certificate_summary, STRING));
        setIconSpaceReserved(false);
        setOnPreferenceClickListener(
                prefs -> {
                    KeyChain.choosePrivateKeyAlias(
                            fragment.requireActivity(),
                            alias ->
                                    fragment.requireActivity().runOnUiThread(() -> setValue(alias)),
                            null,
                            null,
                            null,
                            -1,
                            getPersistedString(null));
                    return true;
                });
    }

    private void setValue(String text) {
        final boolean wasBlocking = shouldDisableDependents();

        persistString(text);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }

        notifyChanged();
    }
}
