package org.ostrya.presencepublisher.preference.connection;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.preference.common.validation.RangeValidator;

public class PortPreference extends TextPreferenceBase {
    public static final String PORT = "port";

    public PortPreference(Context context) {
        super(
                context,
                PORT,
                new RangeValidator(1, 65535),
                R.string.port_title,
                R.string.port_summary);
    }
}
