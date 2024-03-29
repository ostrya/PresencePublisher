package org.ostrya.presencepublisher.mqtt.context.condition.beacon;

import android.content.SharedPreferences;

import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RegionMonitorNotifier implements MonitorNotifier {
    public static final String FOUND_BEACON_LIST = "foundBeacons";
    private static final String TAG = "RegionMonitorNotifier";
    private final SharedPreferences sharedPreferences;

    public RegionMonitorNotifier(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void didEnterRegion(Region region) {
        String regionId = region.getUniqueId();
        Set<String> foundRegions =
                new HashSet<>(
                        sharedPreferences.getStringSet(FOUND_BEACON_LIST, Collections.emptySet()));
        if (!foundRegions.contains(regionId)) {
            DatabaseLogger.i(TAG, "Found " + regionId);
            DatabaseLogger.logDetection("Found beacon: " + regionId);
            foundRegions.add(regionId);
            sharedPreferences.edit().putStringSet(FOUND_BEACON_LIST, foundRegions).apply();
        }
    }

    @Override
    public void didExitRegion(Region region) {
        String regionId = region.getUniqueId();
        Set<String> foundRegions =
                new HashSet<>(
                        sharedPreferences.getStringSet(FOUND_BEACON_LIST, Collections.emptySet()));
        if (foundRegions.contains(regionId)) {
            foundRegions.remove(regionId);
            DatabaseLogger.i(TAG, "Lost " + regionId);
            DatabaseLogger.logDetection("Lost beacon: " + regionId);
            sharedPreferences.edit().putStringSet(FOUND_BEACON_LIST, foundRegions).apply();
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        // ignored
    }
}
