package org.ostrya.presencepublisher.message;

import static org.ostrya.presencepublisher.ui.preference.condition.WifiCategorySupport.SSID_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.WifiCategorySupport.WIFI_CONTENT_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.schedule.PresenceTopicPreference.PRESENCE_TOPIC;

import android.content.Context;

import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.message.wifi.SsidUtil;
import org.ostrya.presencepublisher.ui.preference.condition.WifiNetworkPreference;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class WifiMessageProvider extends AbstractMessageProvider {
    private static final String TAG = "WifiMessageProvider";

    public WifiMessageProvider(Context context) {
        super(context, PRESENCE_TOPIC);
    }

    @Override
    protected List<String> getMessageContents() {
        String ssid = getSsidIfMatching();
        if (ssid != null) {
            HyperLog.i(TAG, "Scheduling message for SSID " + ssid);
            String onlineContent =
                    getSharedPreferences()
                            .getString(
                                    WIFI_CONTENT_PREFIX + ssid,
                                    WifiNetworkPreference.DEFAULT_CONTENT_ONLINE);
            return Collections.singletonList(onlineContent);
        }
        return Collections.emptyList();
    }

    private String getSsidIfMatching() {
        HyperLog.i(TAG, "Checking SSID");
        String ssid = SsidUtil.getCurrentSsid(getApplicationContext());
        if (ssid == null) {
            HyperLog.i(TAG, "No SSID found");
            return null;
        }
        Set<String> targetSsids =
                getSharedPreferences().getStringSet(SSID_LIST, Collections.emptySet());
        if (targetSsids.contains(ssid)) {
            HyperLog.d(TAG, "Correct network found");
            return ssid;
        } else {
            HyperLog.i(
                    TAG,
                    "'"
                            + ssid
                            + "' does not match any desired network '"
                            + targetSsids
                            + "', skipping.");
            return null;
        }
    }
}
