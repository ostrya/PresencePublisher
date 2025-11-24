package org.ostrya.presencepublisher.mqtt.context.condition;

import static org.ostrya.presencepublisher.log.FormatArgs.args;
import static org.ostrya.presencepublisher.preference.condition.BeaconCategorySupport.BEACON_CONTENT_PREFIX;
import static org.ostrya.presencepublisher.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.preference.condition.WifiCategorySupport.SSID_LIST;
import static org.ostrya.presencepublisher.preference.condition.WifiCategorySupport.WIFI_CONTENT_PREFIX;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.PresencePublisher;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.context.condition.beacon.RegionMonitorNotifier;
import org.ostrya.presencepublisher.preference.condition.BeaconPreference;
import org.ostrya.presencepublisher.preference.condition.OfflineContentPreference;
import org.ostrya.presencepublisher.preference.condition.WifiNetwork;
import org.ostrya.presencepublisher.preference.condition.WifiNetworkPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConditionContentProvider {
    private static final String TAG = "ConditionContentProvider";

    private final PresencePublisher applicationContext;
    private final SharedPreferences sharedPreferences;

    public ConditionContentProvider(Context applicationContext) {
        this.applicationContext = (PresencePublisher) applicationContext;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    }

    public List<String> getConditionContents(Set<String> currentSsids) {
        List<String> contents = new ArrayList<>();

        // add beacons
        if (applicationContext.supportsBeacons()) {
            DatabaseLogger.d(TAG, "Checking found beacons");
            for (String beaconId :
                    sharedPreferences.getStringSet(
                            RegionMonitorNotifier.FOUND_BEACON_LIST, Collections.emptySet())) {
                DatabaseLogger.i(TAG, "Adding content for beacon %s", args(beaconId));
                String content =
                        sharedPreferences.getString(
                                BEACON_CONTENT_PREFIX + beaconId,
                                BeaconPreference.DEFAULT_CONTENT_ONLINE);
                contents.add(content);
            }
        }

        // add SSID
        Set<String> ssids = getSsidsWhereMatching(currentSsids);
        if (!ssids.isEmpty()) {
            DatabaseLogger.i(TAG, "Adding content for SSIDs %s", args(ssids));
            ssids.forEach(
                    ssid -> {
                        String onlineContent =
                                sharedPreferences.getString(
                                        WIFI_CONTENT_PREFIX + ssid,
                                        WifiNetworkPreference.DEFAULT_CONTENT_ONLINE);
                        contents.add(onlineContent);
                    });
        }

        // add offline message
        if (contents.isEmpty() && sharedPreferences.getBoolean(SEND_OFFLINE_MESSAGE, false)) {
            DatabaseLogger.i(TAG, "Triggering offline message");
            String offlineContent =
                    sharedPreferences.getString(
                            OfflineContentPreference.OFFLINE_CONTENT,
                            OfflineContentPreference.DEFAULT_CONTENT_OFFLINE);
            contents.add(offlineContent);
        }

        return contents;
    }

    private Set<String> getSsidsWhereMatching(Set<String> currentSsids) {
        DatabaseLogger.d(TAG, "Checking SSIDs");
        if (currentSsids.isEmpty()) {
            DatabaseLogger.i(TAG, "No SSID found");
            return Collections.emptySet();
        }
        Set<String> matching = findMatchingNetworks(currentSsids);
        if (!matching.isEmpty()) {
            DatabaseLogger.d(TAG, "Matching networks found");
        } else {
            DatabaseLogger.i(
                    TAG,
                    "No SSID in '%s' matches any desired network, skipping.",
                    args(currentSsids));
        }
        return matching;
    }

    private Set<String> findMatchingNetworks(Set<String> currentSsids) {
        Set<String> targetSsids = sharedPreferences.getStringSet(SSID_LIST, Collections.emptySet());
        Set<String> matching = new HashSet<>();
        for (String target : targetSsids) {
            WifiNetwork network = WifiNetwork.fromRawString(target);
            if (network != null) {
                currentSsids.forEach(
                        ssid -> {
                            if (network.matches(ssid)) {
                                matching.add(ssid);
                            }
                        });
            }
        }
        return matching;
    }
}
