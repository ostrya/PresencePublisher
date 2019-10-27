package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.ostrya.presencepublisher.R;

public class UseTlsPreference extends AbstractBooleanPreference {
    public static final String USE_TLS = "tls";

    public UseTlsPreference(Context context) {
        super(context, USE_TLS, R.string.use_tls_title, R.string.use_tls_summary);
    }
}
