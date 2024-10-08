package org.ostrya.presencepublisher.mqtt.context.device;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.context.MessageContext;

public class BatteryStatusProvider {
    private static final String TAG = "BatteryStatusProvider";

    private static final BatteryStatus UNKNOWN =
            new BatteryStatus(MessageContext.UNKNOWN, -1, MessageContext.UNKNOWN);

    private final BatteryIntentLoader batteryIntentLoader;

    public BatteryStatusProvider(Context applicationContext) {
        batteryIntentLoader = new BatteryIntentLoader(applicationContext);
    }

    public BatteryStatus getCurrentBatteryStatus() {
        Intent batteryStatusIntent = batteryIntentLoader.getBatteryIntent();
        if (batteryStatusIntent == null) {
            DatabaseLogger.w(TAG, "No battery status received, returning fallback value");
            return UNKNOWN;
        } else {
            int level = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryLevelPercentage;

            if (level == -1 || scale == -1) {
                DatabaseLogger.w(TAG, "Invalid level " + level + " or scale " + scale);
                batteryLevelPercentage = -1;
            } else {
                batteryLevelPercentage = (int) (level / (0.01f * scale));
            }

            int status = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            String batteryStatus;
            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    batteryStatus = "CHARGING";
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    batteryStatus = "DISCHARGING";
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    batteryStatus = "FULL";
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    batteryStatus = "NOT_CHARGING";
                    break;
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                case -1:
                default:
                    batteryStatus = MessageContext.UNKNOWN;
                    break;
            }

            int plugged = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            String plugStatus;
            switch (plugged) {
                case BatteryManager.BATTERY_PLUGGED_AC:
                    plugStatus = "AC";
                    break;
                case BatteryManager.BATTERY_PLUGGED_USB:
                    plugStatus = "USB";
                    break;
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    plugStatus = "WIRELESS";
                    break;
                default:
                    plugStatus = MessageContext.UNKNOWN;
                    break;
            }
            return new BatteryStatus(batteryStatus, batteryLevelPercentage, plugStatus);
        }
    }
}
