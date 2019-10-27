package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

public class WifiContentPreference extends AbstractTextPreference {
    public static final String WIFI_CONTENT_PREFIX = "wifi.";
    public static final String DEFAULT_CONTENT_ONLINE = "online";

    public WifiContentPreference(Context context, String ssid) {
        super(context, WIFI_CONTENT_PREFIX + ssid, new RegexValidator(".+"), ssid, R.string.content_summary);
        setDefaultValue(DEFAULT_CONTENT_ONLINE);
    }
}
