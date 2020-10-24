package org.ostrya.presencepublisher.ui.preference.about;

import android.content.Context;
import org.ostrya.presencepublisher.BuildConfig;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.StringDummy;

public class VersionNamePreference extends StringDummy {
    public VersionNamePreference(Context context) {
        super(context, R.string.version_name_title, BuildConfig.VERSION_NAME);
    }
}
