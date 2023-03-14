package org.ostrya.presencepublisher.beacon;

import static org.ostrya.presencepublisher.beacon.RegionMonitorNotifier.FOUND_BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_LIST;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PresenceBeaconManager {
    private static final String TAG = "PresenceBeaconManager";
    private static final PresenceBeaconManager INSTANCE = new PresenceBeaconManager();

    @GuardedBy("this")
    @Nullable
    private BeaconManager beaconManager;

    private PresenceBeaconManager() {}

    public static PresenceBeaconManager getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize(Context context) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext);
        List<Region> configuredScanRegions = getConfiguredScanRegions(sharedPreferences);
        RegionMonitorNotifier monitorNotifier = new RegionMonitorNotifier(sharedPreferences);
        beaconManager = BeaconManager.getInstanceForApplication(applicationContext);
        beaconManager.addMonitorNotifier(monitorNotifier);
        for (Region region : configuredScanRegions) {
            beaconManager.startMonitoring(region);
        }
    }

    private List<Region> getConfiguredScanRegions(SharedPreferences sharedPreferences) {
        Set<String> beaconList =
                sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet());
        List<Region> scanRegions = new ArrayList<>(beaconList.size());
        for (String beaconId : beaconList) {
            String address = PresenceBeacon.beaconIdToAddress(beaconId);
            DatabaseLogger.d(TAG, "Registering scan region for beacon " + beaconId);
            scanRegions.add(new Region(beaconId, address));
        }
        return scanRegions;
    }

    public synchronized void addBeacon(Context context, PresenceBeacon beacon) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext);
        Set<String> storedBeacons =
                new HashSet<>(sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet()));
        String beaconId = beacon.toBeaconId();
        storedBeacons.add(beaconId);
        sharedPreferences.edit().putStringSet(BEACON_LIST, storedBeacons).apply();

        DatabaseLogger.d(TAG, "Add scanning for beacon " + beaconId);
        Region region = new Region(beaconId, beacon.getAddress());
        if (beaconManager == null) {
            DatabaseLogger.d(TAG, "Start scanning for beacons");
            beaconManager = BeaconManager.getInstanceForApplication(applicationContext);
            RegionMonitorNotifier monitorNotifier = new RegionMonitorNotifier(sharedPreferences);
            beaconManager.addMonitorNotifier(monitorNotifier);
        }
        beaconManager.startMonitoring(region);
    }

    public synchronized void removeBeacon(Context context, String beaconId) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext);
        Set<String> storedBeacons =
                new HashSet<>(sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet()));
        storedBeacons.remove(beaconId);
        Set<String> foundBeacons =
                new HashSet<>(
                        sharedPreferences.getStringSet(FOUND_BEACON_LIST, Collections.emptySet()));
        foundBeacons.remove(beaconId);
        sharedPreferences
                .edit()
                .putStringSet(BEACON_LIST, storedBeacons)
                .putStringSet(FOUND_BEACON_LIST, foundBeacons)
                .apply();

        DatabaseLogger.d(TAG, "Remove scanning for beacon " + beaconId);
        Region region = new Region(beaconId, PresenceBeacon.beaconIdToAddress(beaconId));
        if (beaconManager != null) {
            beaconManager.stopMonitoring(region);
            if (storedBeacons.isEmpty()) {
                DatabaseLogger.i(TAG, "Disable scanning for beacons");
                beaconManager.removeAllMonitorNotifiers();
                beaconManager.shutdownIfIdle();
                beaconManager = null;
            }
        }
    }
}
