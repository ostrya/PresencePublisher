package org.ostrya.presencepublisher.ui.preference.about;

import android.content.Context;
import org.ostrya.presencepublisher.BuildConfig;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.StringDummy;

public class AppIdPreferenceDummy extends StringDummy {
    public AppIdPreferenceDummy(Context context) {
        super(context, R.string.app_id_title, BuildConfig.APPLICATION_ID);
    }
}
