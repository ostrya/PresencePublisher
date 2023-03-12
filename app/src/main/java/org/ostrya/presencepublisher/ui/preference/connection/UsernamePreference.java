package org.ostrya.presencepublisher.ui.preference.connection;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.ui.util.NonEmptyStringValidator;

public class UsernamePreference extends TextPreferenceBase {
    public static final String USERNAME = "login";

    public UsernamePreference(Context context) {
        super(
                context,
                USERNAME,
                new NonEmptyStringValidator(),
                R.string.username_title,
                R.string.username_summary);
    }
}
