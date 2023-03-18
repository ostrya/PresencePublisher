package org.ostrya.presencepublisher.mqtt.context.condition.beacon;

import org.altbeacon.beacon.Beacon;

import java.util.Objects;

public class PresenceBeacon {
    private static final String BEACON_FORMAT = "%s (%s)%n%s";

    private final String address;
    private final String name;
    private final String type;
    private final int rssi;
    private final double distance;

    public PresenceBeacon(Beacon beacon) {
        Objects.requireNonNull(beacon);
        address = Objects.requireNonNull(beacon.getBluetoothAddress());
        if (beacon.getBluetoothName() != null) {
            name = beacon.getBluetoothName();
        } else {
            name = address;
        }
        type = Objects.requireNonNull(beacon.getParserIdentifier());
        rssi = beacon.getRssi();
        distance = beacon.getDistance();
    }

    public static String beaconIdToAddress(String beaconId) {
        return beaconId.substring(beaconId.lastIndexOf('\n') + 1);
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getRssi() {
        return rssi;
    }

    public double getDistance() {
        return distance;
    }

    public String toBeaconId() {
        return String.format(BEACON_FORMAT, name, type, address);
    }
}
