package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import org.ostrya.presencepublisher.ui.preference.CheckConnectionDummy;
import org.ostrya.presencepublisher.ui.preference.ClientCertificatePreference;
import org.ostrya.presencepublisher.ui.preference.HostPreference;
import org.ostrya.presencepublisher.ui.preference.PasswordPreference;
import org.ostrya.presencepublisher.ui.preference.PortPreference;
import org.ostrya.presencepublisher.ui.preference.UseTlsPreference;
import org.ostrya.presencepublisher.ui.preference.UsernamePreference;

import static org.ostrya.presencepublisher.ui.preference.HostPreference.HOST;
import static org.ostrya.presencepublisher.ui.preference.UseTlsPreference.USE_TLS;

public class ConnectionFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference host = new HostPreference(context);
        Preference port = new PortPreference(context);

        Preference username = new UsernamePreference(context);
        Preference password = new PasswordPreference(context);

        Preference useTls = new UseTlsPreference(context);
        Preference clientCertificate = new ClientCertificatePreference(context, this);

        Preference checkConnection = new CheckConnectionDummy(context, this);

        screen.addPreference(host);
        screen.addPreference(port);
        screen.addPreference(username);
        screen.addPreference(password);
        screen.addPreference(useTls);
        screen.addPreference(clientCertificate);
        screen.addPreference(checkConnection);

        setPreferenceScreen(screen);

        checkConnection.setDependency(HOST);
        clientCertificate.setDependency(USE_TLS);
    }
}
