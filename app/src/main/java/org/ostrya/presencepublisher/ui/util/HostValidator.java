package org.ostrya.presencepublisher.ui.util;

import android.content.Context;

import androidx.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.internal.NetworkModuleService;

import java.net.URI;
import java.net.URISyntaxException;

public class HostValidator implements Validator {
    @Override
    public boolean isValid(Context context, @Nullable String key, String value) {
        try {
            String testUri = "tcp://" + value;
            NetworkModuleService.validateURI(testUri);
            return new URI(testUri).getHost() != null;
        } catch (IllegalArgumentException | URISyntaxException e) {
            return false;
        }
    }
}
