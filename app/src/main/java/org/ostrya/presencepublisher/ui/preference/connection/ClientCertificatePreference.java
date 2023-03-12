package org.ostrya.presencepublisher.ui.preference.connection;

import android.content.Context;
import android.security.KeyChain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;

import org.ostrya.presencepublisher.R;

public class ClientCertificatePreference extends Preference
        implements Preference.SummaryProvider<ClientCertificatePreference> {
    public static final String CLIENT_CERTIFICATE = "client_cert";

    public ClientCertificatePreference(Context context, Fragment fragment) {
        super(context);
        setKey(CLIENT_CERTIFICATE);
        setTitle(R.string.client_certificate_title);
        setSummaryProvider(this);
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

    @Nullable
    @Override
    public CharSequence provideSummary(@NonNull ClientCertificatePreference preference) {
        return preference
                .getContext()
                .getString(R.string.client_certificate_summary, preference.getValue());
    }

    private String getValue() {
        String undefined = getContext().getString(R.string.value_undefined);
        return getPersistedString(undefined);
    }
}
