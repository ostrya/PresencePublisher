package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

public class OfflineContentPreference extends AbstractTextPreference {
    public static final String OFFLINE_CONTENT = "offlineContent";
    public static final String DEFAULT_CONTENT_OFFLINE = "offline";

    public OfflineContentPreference(Context context) {
        super(context, OFFLINE_CONTENT, new RegexValidator(".+"), R.string.offline_content_title, R.string.content_summary);
        setDefaultValue(DEFAULT_CONTENT_OFFLINE);
    }
}
