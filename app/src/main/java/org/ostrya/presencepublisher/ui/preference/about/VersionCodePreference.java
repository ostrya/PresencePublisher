package org.ostrya.presencepublisher.ui.preference.about;

import android.content.Context;
import org.ostrya.presencepublisher.BuildConfig;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.StringDummy;

public class VersionCodePreference extends StringDummy {
    public VersionCodePreference(Context context) {
        super(context, R.string.version_code_title, String.valueOf(BuildConfig.VERSION_CODE));
    }
}
