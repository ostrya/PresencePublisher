package org.ostrya.presencepublisher.ui.preference.connection;

import static org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider.PreferenceType.STRING;

import android.content.Context;

import org.eclipse.paho.client.mqttv3.internal.NetworkModuleService;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;

import java.net.URI;
import java.net.URISyntaxException;

public class HostPreference extends TextPreferenceBase {
    public static final String HOST = "host";

    public HostPreference(Context context) {
        super(
                context,
                HOST,
                value -> {
                    try {
                        String testUri = "tcp://" + value;
                        NetworkModuleService.validateURI(testUri);
                        return new URI(testUri).getHost() != null;
                    } catch (IllegalArgumentException | URISyntaxException e) {
                        return false;
                    }
                },
                R.string.host_title);
    }

    @Override
    protected void configureSummary() {
        setSummaryProvider(new ExplanationSummaryProvider<>(R.string.host_summary, STRING));
    }
}
