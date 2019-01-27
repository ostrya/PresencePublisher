package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.os.Bundle;
import android.security.KeyChain;
import androidx.preference.*;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.CheckConnectionDialogFragment;
import org.ostrya.presencepublisher.ui.util.CustomPreference;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;
import org.ostrya.presencepublisher.ui.util.RangeValidator;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

import static org.ostrya.presencepublisher.ui.dialog.CheckConnectionDialogFragment.getInstance;
import static org.ostrya.presencepublisher.ui.util.EditTextPreferencesHelper.getEditTextPreference;

public class ConnectionFragment extends PreferenceFragmentCompat {

    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String TLS = "tls";
    public static final String CLIENT_CERT = "client_cert";
    public static final String TOPIC = "topic";
    public static final String PING = "ping";

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        EditTextPreference host = getEditTextPreference(context, HOST, R.string.host_title, R.string.host_summary, new RegexValidator("[^:/]+"));
        EditTextPreference port = getEditTextPreference(context, PORT, R.string.port_title, R.string.port_summary, new RangeValidator(1025, 65535));

        SwitchPreferenceCompat tls = new SwitchPreferenceCompat(context);
        tls.setKey(TLS);
        tls.setTitle(getString(R.string.tls_title));
        tls.setSummary(R.string.tls_summary);
        tls.setIconSpaceReserved(false);

        CustomPreference clientCert = new CustomPreference(context);
        clientCert.setKey(CLIENT_CERT);
        clientCert.setTitle(context.getString(R.string.client_cert_title));
        clientCert.setSummaryProvider(new ExplanationSummaryProvider(R.string.client_cert_summary));
        clientCert.setOnPreferenceClickListener(prefs -> {
            KeyChain.choosePrivateKeyAlias(
                    requireActivity(),
                    alias -> requireActivity().runOnUiThread(() -> ((CustomPreference) prefs).setValue(alias)),
                    null,
                    null,
                    null,
                    -1,
                    ((CustomPreference) prefs).getValue(null)
            );
            return true;
        });
        clientCert.setIconSpaceReserved(false);

        EditTextPreference topic = getEditTextPreference(context, TOPIC, R.string.topic_title, R.string.topic_summary, new RegexValidator("[^ ]+"));

        Preference checkConnection = new Preference(context);
        checkConnection.setTitle(R.string.check_connection_title);
        checkConnection.setSummary(R.string.check_connection_summary);
        checkConnection.setOnPreferenceClickListener(prefs -> {
            CheckConnectionDialogFragment fragment = getInstance(context, getPreferenceManager().getSharedPreferences());
            fragment.show(requireFragmentManager(), null);
            return true;
        });
        checkConnection.setIconSpaceReserved(false);

        screen.addPreference(host);
        screen.addPreference(port);
        screen.addPreference(tls);
        screen.addPreference(clientCert);
        screen.addPreference(topic);
        screen.addPreference(checkConnection);

        setPreferenceScreen(screen);

        clientCert.setDependency(TLS);
    }
}
