package org.ostrya.presencepublisher.mqtt.context.device;

import org.ostrya.presencepublisher.mqtt.context.MessageContext;

public class BatteryStatus {
    private final String batteryStatus;
    private final int batteryLevelPercentage;
    private final String plugStatus;

    public BatteryStatus(String batteryStatus, int batteryLevelPercentage, String plugStatus) {
        this.batteryStatus = batteryStatus;
        this.batteryLevelPercentage = batteryLevelPercentage;
        this.plugStatus = plugStatus;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public int getBatteryLevelPercentage() {
        return batteryLevelPercentage;
    }

    public String getPlugStatus() {
        return plugStatus;
    }

    public boolean isCharging() {
        return !MessageContext.UNKNOWN.equals(plugStatus);
    }
}
