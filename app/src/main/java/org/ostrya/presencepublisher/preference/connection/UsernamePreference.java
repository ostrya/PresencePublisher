package org.ostrya.presencepublisher.preference.connection;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.preference.common.validation.NonEmptyStringValidator;

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
