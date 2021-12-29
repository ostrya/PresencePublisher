package org.ostrya.presencepublisher.message;

import static org.ostrya.presencepublisher.beacon.RegionMonitorNotifier.FOUND_BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_CONTENT_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.WifiCategorySupport.SSID_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.WifiCategorySupport.WIFI_CONTENT_PREFIX;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.VisibleForTesting;
import androidx.preference.PreferenceManager;

import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.PresencePublisher;
import org.ostrya.presencepublisher.network.NetworkService;
import org.ostrya.presencepublisher.ui.preference.condition.BeaconPreference;
import org.ostrya.presencepublisher.ui.preference.condition.OfflineContentPreference;
import org.ostrya.presencepublisher.ui.preference.condition.WifiNetworkPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ConditionContentProvider {
    private static final String TAG = "ConditionContentProvider";

    private static final String KEY = MessageItem.CONDITION_CONTENT.getName();

    private final PresencePublisher applicationContext;
    private final SharedPreferences sharedPreferences;
    private final NetworkService networkService;

    public ConditionContentProvider(Context applicationContext) {
        this.applicationContext = (PresencePublisher) applicationContext;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        this.networkService = new NetworkService(applicationContext, sharedPreferences);
    }

    @VisibleForTesting
    ConditionContentProvider(
            PresencePublisher applicationContext,
            SharedPreferences sharedPreferences,
            NetworkService networkService) {
        this.applicationContext = applicationContext;
        this.sharedPreferences = sharedPreferences;
        this.networkService = networkService;
    }

    public ListEntry getConditionContents() {
        List<String> contents = new ArrayList<>();

        // add beacons
        if (applicationContext.supportsBeacons()) {
            HyperLog.i(TAG, "Checking found beacons");
            for (String beaconId :
                    sharedPreferences.getStringSet(FOUND_BEACON_LIST, Collections.emptySet())) {
                HyperLog.i(TAG, "Adding content for beacon " + beaconId);
                String content =
                        sharedPreferences.getString(
                                BEACON_CONTENT_PREFIX + beaconId,
                                BeaconPreference.DEFAULT_CONTENT_ONLINE);
                contents.add(content);
            }
        }

        // add SSID
        String ssid = getSsidIfMatching();
        if (ssid != null) {
            HyperLog.i(TAG, "Adding content for SSID " + ssid);
            String onlineContent =
                    sharedPreferences.getString(
                            WIFI_CONTENT_PREFIX + ssid,
                            WifiNetworkPreference.DEFAULT_CONTENT_ONLINE);
            contents.add(onlineContent);
        }

        // add offline message
        if (contents.isEmpty() && sharedPreferences.getBoolean(SEND_OFFLINE_MESSAGE, false)) {
            HyperLog.i(TAG, "Scheduling offline message");
            String offlineContent =
                    sharedPreferences.getString(
                            OfflineContentPreference.OFFLINE_CONTENT,
                            OfflineContentPreference.DEFAULT_CONTENT_OFFLINE);
            contents.add(offlineContent);
        }

        return new ListEntry(KEY, contents);
    }

    private String getSsidIfMatching() {
        HyperLog.i(TAG, "Checking SSID");
        String ssid = networkService.getCurrentSsid();
        if (ssid == null) {
            HyperLog.i(TAG, "No SSID found");
            return null;
        }
        Set<String> targetSsids = sharedPreferences.getStringSet(SSID_LIST, Collections.emptySet());
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
