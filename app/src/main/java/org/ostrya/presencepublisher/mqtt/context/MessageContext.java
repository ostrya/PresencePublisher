package org.ostrya.presencepublisher.mqtt.context;

import androidx.annotation.Nullable;

import org.ostrya.presencepublisher.mqtt.context.battery.BatteryStatus;

import java.util.List;

public class MessageContext {
    public static final String UNKNOWN = "N/A";

    private final long nextAlarmclockTimestamp;
    private final BatteryStatus batteryStatus;
    private final List<String> conditionContents;
    private final String lastKnownLocation;
    @Nullable private final String currentSsid;
    private final long lastSuccessTimestamp;
    private final long currentTimestamp;
    private final long estimatedNextTimestamp;

    MessageContext(
            long nextAlarmclockTimestamp,
            BatteryStatus batteryStatus,
            List<String> conditionContents,
            String lastKnownLocation,
            long lastSuccessTimestamp,
            long currentTimestamp,
            long estimatedNextTimestamp,
            @Nullable String currentSsid) {
        this.nextAlarmclockTimestamp = nextAlarmclockTimestamp;
        this.batteryStatus = batteryStatus;
        this.conditionContents = conditionContents;
        this.lastKnownLocation = lastKnownLocation;
        this.lastSuccessTimestamp = lastSuccessTimestamp;
        this.currentTimestamp = currentTimestamp;
        this.estimatedNextTimestamp = estimatedNextTimestamp;
        this.currentSsid = currentSsid;
    }

    public long getNextAlarmclockTimestamp() {
        return nextAlarmclockTimestamp;
    }

    public BatteryStatus getBatteryStatus() {
        return batteryStatus;
    }

    public List<String> getConditionContents() {
        return conditionContents;
    }

    public String getLastKnownLocation() {
        return lastKnownLocation;
    }

    @Nullable
    public String getCurrentSsid() {
        return currentSsid;
    }

    public long getLastSuccessTimestamp() {
        return lastSuccessTimestamp;
    }

    public long getCurrentTimestamp() {
        return currentTimestamp;
    }

    public long getEstimatedNextTimestamp() {
        return estimatedNextTimestamp;
    }
}
