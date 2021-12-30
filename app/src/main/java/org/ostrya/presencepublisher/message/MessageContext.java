package org.ostrya.presencepublisher.message;

import androidx.annotation.Nullable;

public class MessageContext {

    public static final String UNKNOWN = "N/A";

    private final BatteryStatusProvider batteryStatusProvider;
    private final ConditionContentProvider conditionContentProvider;
    private final LocationProvider locationProvider;
    @Nullable private final String currentSsid;
    private final long currentTimestamp;
    private final long nextTimestamp;

    public MessageContext(
            BatteryStatusProvider batteryStatusProvider,
            ConditionContentProvider conditionContentProvider,
            LocationProvider locationProvider,
            @Nullable String currentSsid,
            long nextTimestamp) {
        this.batteryStatusProvider = batteryStatusProvider;
        this.conditionContentProvider = conditionContentProvider;
        this.locationProvider = locationProvider;
        this.currentSsid = currentSsid;
        this.currentTimestamp = System.currentTimeMillis();
        this.nextTimestamp = nextTimestamp;
    }

    public BatteryStatusProvider getBatteryStatusProvider() {
        return batteryStatusProvider;
    }

    public ConditionContentProvider getConditionContentProvider() {
        return conditionContentProvider;
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    @Nullable
    public String getCurrentSsid() {
        return currentSsid;
    }

    public long getCurrentTimestamp() {
        return currentTimestamp;
    }

    public long getNextTimestamp() {
        return nextTimestamp;
    }
}
