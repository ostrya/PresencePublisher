package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

public class UsernamePreference extends AbstractTextPreference {
    public static final String USERNAME = "login";

    public UsernamePreference(Context context) {
        super(context, USERNAME, new RegexValidator("[^ ]*"), R.string.username_title, R.string.username_summary);
    }
}
