package org.ostrya.presencepublisher.ui.preference.connection;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.BooleanPreferenceBase;

public class UseTlsPreference extends BooleanPreferenceBase {
    public static final String USE_TLS = "tls";

    public UseTlsPreference(Context context) {
        super(context, USE_TLS, R.string.use_tls_title, R.string.use_tls_summary);
    }
}
