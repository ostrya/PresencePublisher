package org.ostrya.presencepublisher.mqtt.context.condition;

import static org.ostrya.presencepublisher.preference.condition.BeaconCategorySupport.BEACON_CONTENT_PREFIX;
import static org.ostrya.presencepublisher.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.preference.condition.WifiCategorySupport.SSID_LIST;
import static org.ostrya.presencepublisher.preference.condition.WifiCategorySupport.WIFI_CONTENT_PREFIX;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
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

    public List<String> getConditionContents(@Nullable String currentSsid) {
        List<String> contents = new ArrayList<>();

        // add beacons
        if (applicationContext.supportsBeacons()) {
            DatabaseLogger.d(TAG, "Checking found beacons");
            for (String beaconId :
                    sharedPreferences.getStringSet(
                            RegionMonitorNotifier.FOUND_BEACON_LIST, Collections.emptySet())) {
                DatabaseLogger.i(TAG, "Adding content for beacon " + beaconId);
                String content =
                        sharedPreferences.getString(
                                BEACON_CONTENT_PREFIX + beaconId,
                                BeaconPreference.DEFAULT_CONTENT_ONLINE);
                contents.add(content);
            }
        }

        // add SSID
        String ssid = getSsidIfMatching(currentSsid);
        if (ssid != null) {
            DatabaseLogger.i(TAG, "Adding content for SSID " + ssid);
            String onlineContent =
                    sharedPreferences.getString(
                            WIFI_CONTENT_PREFIX + ssid,
                            WifiNetworkPreference.DEFAULT_CONTENT_ONLINE);
            contents.add(onlineContent);
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

    private String getSsidIfMatching(String currentSsid) {
        DatabaseLogger.d(TAG, "Checking SSID");
        if (currentSsid == null) {
            DatabaseLogger.i(TAG, "No SSID found");
            return null;
        }
        if (networkMatches(currentSsid)) {
            DatabaseLogger.d(TAG, "Correct network found");
            return currentSsid;
        } else {
            DatabaseLogger.i(
                    TAG, "'" + currentSsid + "' does not match any desired network, skipping.");
            return null;
        }
    }

    private boolean networkMatches(String currentSsid) {
        Set<String> targetSsids = sharedPreferences.getStringSet(SSID_LIST, Collections.emptySet());
        for (String target : targetSsids) {
            WifiNetwork network = WifiNetwork.fromRawString(target);
            if (network != null && network.matches(currentSsid)) {
                return true;
            }
        }
        return false;
    }
}
