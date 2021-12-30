package org.ostrya.presencepublisher.ui.preference.connection;


import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

public class UsernamePreference extends TextPreferenceBase {
    public static final String USERNAME = "login";

    public UsernamePreference(Context context) {
        super(context, USERNAME, new RegexValidator("[^ ]*"), R.string.username_title);
    }

    @Override
    protected void configureSummary() {
        setSummaryProvider(new ExplanationSummaryProvider<>(R.string.username_summary));
    }
}
