package org.ostrya.presencepublisher.preference.about;

import android.content.Context;

import org.ostrya.presencepublisher.BuildConfig;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.StringDummy;

public class VersionInfoPreferenceDummy extends StringDummy {
    public VersionInfoPreferenceDummy(Context context) {
        super(
                context,
                R.string.version_title,
                context.getString(
                        R.string.version_summary,
                        BuildConfig.APPLICATION_ID,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE,
                        BuildConfig.BUILD_TYPE));
    }
}
