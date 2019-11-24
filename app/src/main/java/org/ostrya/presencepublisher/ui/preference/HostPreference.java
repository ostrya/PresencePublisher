package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.eclipse.paho.client.mqttv3.internal.NetworkModuleService;
import org.ostrya.presencepublisher.R;

import java.net.URI;
import java.net.URISyntaxException;

public class HostPreference extends AbstractTextPreference {
    public static final String HOST = "host";

    public HostPreference(Context context) {
        super(context, HOST, value -> {
            try {
                String testUri = "tcp://" + value;
                NetworkModuleService.validateURI(testUri);
                return new URI(testUri).getHost() != null;
            } catch (IllegalArgumentException | URISyntaxException e) {
                return false;
            }
        }, R.string.host_title, R.string.host_summary);
    }
}
