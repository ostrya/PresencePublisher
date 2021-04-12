package org.ostrya.presencepublisher.ui.preference.condition;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.AbstractDynamicPreferenceCategorySupport;
import org.ostrya.presencepublisher.ui.util.AbstractConfigurationFragment;

public class WifiCategorySupport extends AbstractDynamicPreferenceCategorySupport {
    public static final String SSID_LIST = "ssids";
    public static final String WIFI_CONTENT_PREFIX = "wifi.";

    public WifiCategorySupport(AbstractConfigurationFragment fragment) {
        super(fragment, R.string.category_wifi, SSID_LIST, WIFI_CONTENT_PREFIX, AddNetworkChoicePreferenceDummy::new, WifiNetworkPreference::new);
    }
}
