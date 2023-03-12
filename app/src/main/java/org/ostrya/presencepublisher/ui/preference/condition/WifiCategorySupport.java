package org.ostrya.presencepublisher.ui.preference.condition;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.AbstractDynamicPreferenceCategorySupport;
import org.ostrya.presencepublisher.ui.util.AbstractConfigurationFragment;

public class WifiCategorySupport extends AbstractDynamicPreferenceCategorySupport {
    public static final String SSID_LIST = "ssids";
    public static final String WIFI_CONTENT_PREFIX = "wifi.";

    public WifiCategorySupport(AbstractConfigurationFragment fragment) {
        super(
                fragment,
                R.string.category_wifi,
                SSID_LIST,
                WIFI_CONTENT_PREFIX,
                AddNetworkChoicePreferenceDummy::new,
                WifiCategorySupport::createPreference);
    }

    private static WifiNetworkPreference createPreference(
            Context context,
            String key,
            String title,
            SharedPreferences preferences,
            Fragment fragment) {
        WifiNetwork network = WifiNetwork.fromRawString(title);
        return new WifiNetworkPreference(context, key, network, preferences, fragment);
    }
}
