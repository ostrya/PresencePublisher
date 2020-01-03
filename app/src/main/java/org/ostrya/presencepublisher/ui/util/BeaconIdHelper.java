package org.ostrya.presencepublisher.ui.util;

import org.altbeacon.beacon.Beacon;

public final class BeaconIdHelper {
    private static final String BEACON_FORMAT = "%s (%s)%n%s";

    private BeaconIdHelper() {
    }

    public static String toBeaconId(Beacon beacon) {
        return String.format(BEACON_FORMAT, beacon.getBluetoothName(), beacon.getParserIdentifier(), beacon.getBluetoothAddress());
    }

    public static String toAddress(String beaconId) {
        return beaconId.substring(beaconId.lastIndexOf('\n') + 1);
    }
}
