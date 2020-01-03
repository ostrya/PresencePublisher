package org.ostrya.presencepublisher.beacon;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.ostrya.presencepublisher.ui.util.BeaconIdHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ostrya.presencepublisher.beacon.RegionMonitorNotifier.FOUND_BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.AddBeaconChoicePreferenceDummy.BEACON_LIST;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public final class BeaconManager {
    private static final String TAG = "BeaconManager";
    private static final BeaconManager INSTANCE = new BeaconManager();

    @GuardedBy("this")
    @Nullable
    private RegionBootstrap regionBootstrap;

    private BeaconManager() {
    }

    public static BeaconManager getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize(Context context) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        List<Region> configuredScanRegions = getConfiguredScanRegions(sharedPreferences);
        if (configuredScanRegions.isEmpty()) {
            HyperLog.i(TAG, "No beacons configured, not enabling beacon scanning");
            return;
        }
        RegionMonitorNotifier monitorNotifier = new RegionMonitorNotifier(sharedPreferences);
        regionBootstrap = new RegionBootstrap(applicationContext, monitorNotifier, configuredScanRegions);
    }

    private List<Region> getConfiguredScanRegions(SharedPreferences sharedPreferences) {
        Set<String> beaconList = sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet());
        List<Region> scanRegions = new ArrayList<>(beaconList.size());
        for (String beaconId : beaconList) {
            String address = BeaconIdHelper.toAddress(beaconId);
            HyperLog.d(TAG, "Registering scan region for beacon " + beaconId);
            scanRegions.add(new Region(beaconId, address));
        }
        return scanRegions;
    }

    public synchronized void addBeacon(Context context, Beacon beacon) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        Set<String> storedBeacons = new HashSet<>(sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet()));
        String beaconId = BeaconIdHelper.toBeaconId(beacon);
        storedBeacons.add(beaconId);
        sharedPreferences.edit().putStringSet(BEACON_LIST, storedBeacons).apply();

        HyperLog.d(TAG, "Add scanning for beacon " + beaconId);
        Region region = new Region(beaconId, beacon.getBluetoothAddress());
        if (regionBootstrap == null) {
            HyperLog.d(TAG, "Start scanning for beacons");
            RegionMonitorNotifier monitorNotifier = new RegionMonitorNotifier(sharedPreferences);
            regionBootstrap = new RegionBootstrap(applicationContext, monitorNotifier, region);
        } else {
            regionBootstrap.addRegion(region);
        }
    }

    public synchronized void removeBeacon(Context context, String beaconId) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        Set<String> storedBeacons = new HashSet<>(sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet()));
        storedBeacons.remove(beaconId);
        Set<String> foundBeacons = new HashSet<>(sharedPreferences.getStringSet(FOUND_BEACON_LIST, Collections.emptySet()));
        foundBeacons.remove(beaconId);
        sharedPreferences.edit()
                .putStringSet(BEACON_LIST, storedBeacons)
                .putStringSet(FOUND_BEACON_LIST, foundBeacons)
                .apply();

        HyperLog.d(TAG, "Remove scanning for beacon " + beaconId);
        Region region = new Region(beaconId, BeaconIdHelper.toAddress(beaconId));
        if (regionBootstrap != null) {
            if (storedBeacons.isEmpty()) {
                HyperLog.i(TAG, "Disable scanning for beacons");
                regionBootstrap.disable();
                regionBootstrap = null;
            } else {
                regionBootstrap.removeRegion(region);
            }
        }
    }
}
