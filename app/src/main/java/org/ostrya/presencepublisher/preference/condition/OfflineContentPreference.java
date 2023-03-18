package org.ostrya.presencepublisher.preference.condition;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.preference.common.validation.NonEmptyStringValidator;

public class OfflineContentPreference extends TextPreferenceBase {
    public static final String OFFLINE_CONTENT = "offlineContent";
    public static final String DEFAULT_CONTENT_OFFLINE = "offline";

    public OfflineContentPreference(Context context) {
        super(
                context,
                OFFLINE_CONTENT,
                new NonEmptyStringValidator(),
                R.string.offline_content_title,
                R.string.content_summary);
        setDefaultValue(DEFAULT_CONTENT_OFFLINE);
    }
}
