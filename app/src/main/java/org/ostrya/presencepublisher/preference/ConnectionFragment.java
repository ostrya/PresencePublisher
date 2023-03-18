package org.ostrya.presencepublisher.preference;

import static org.ostrya.presencepublisher.preference.connection.HostPreference.HOST;
import static org.ostrya.presencepublisher.preference.connection.UseTlsPreference.USE_TLS;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import org.ostrya.presencepublisher.preference.connection.CheckConnectionDummy;
import org.ostrya.presencepublisher.preference.connection.ClientCertificatePreference;
import org.ostrya.presencepublisher.preference.connection.HostPreference;
import org.ostrya.presencepublisher.preference.connection.PasswordPreference;
import org.ostrya.presencepublisher.preference.connection.PortPreference;
import org.ostrya.presencepublisher.preference.connection.QoSPreference;
import org.ostrya.presencepublisher.preference.connection.RetainFlagPreference;
import org.ostrya.presencepublisher.preference.connection.UseTlsPreference;
import org.ostrya.presencepublisher.preference.connection.UsernamePreference;

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

        Preference qos = new QoSPreference(context);
        Preference retainFlag = new RetainFlagPreference(context);

        Preference checkConnection = new CheckConnectionDummy(context, this);

        screen.addPreference(host);
        screen.addPreference(port);
        screen.addPreference(username);
        screen.addPreference(password);
        screen.addPreference(useTls);
        screen.addPreference(clientCertificate);
        screen.addPreference(qos);
        screen.addPreference(retainFlag);
        screen.addPreference(checkConnection);

        setPreferenceScreen(screen);

        checkConnection.setDependency(HOST);
        clientCertificate.setDependency(USE_TLS);
    }
}
