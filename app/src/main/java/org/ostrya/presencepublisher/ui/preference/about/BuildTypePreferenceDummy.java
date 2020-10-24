package org.ostrya.presencepublisher.ui.preference.about;

import android.content.Context;
import org.ostrya.presencepublisher.BuildConfig;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.StringDummy;

public class BuildTypePreferenceDummy extends StringDummy {
    public BuildTypePreferenceDummy(Context context) {
        super(context, R.string.build_type_title, BuildConfig.BUILD_TYPE);
    }
}
