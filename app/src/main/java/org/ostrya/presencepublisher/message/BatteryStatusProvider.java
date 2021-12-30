package org.ostrya.presencepublisher.message;

import static org.ostrya.presencepublisher.message.MessageContext.UNKNOWN;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import androidx.annotation.Nullable;

import com.hypertrack.hyperlog.HyperLog;

public class BatteryStatusProvider {
    private static final String TAG = "BatteryStatusProvider";

    private final String batteryStatus;
    private final int batteryLevelPercentage;
    private final String plugStatus;

    public BatteryStatusProvider(Context applicationContext) {
        Intent batteryStatusIntent = getBatteryStatusIntent(applicationContext);
        if (batteryStatusIntent == null) {
            HyperLog.w(TAG, "No battery status received, returning fallback value");
            batteryStatus = UNKNOWN;
            batteryLevelPercentage = -1;
            plugStatus = UNKNOWN;
        } else {
            int level = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if (level == -1 || scale == -1) {
                HyperLog.w(TAG, "Invalid level " + level + " or scale " + scale);
                batteryLevelPercentage = -1;
            } else {
                batteryLevelPercentage = (int) (level / (0.01f * scale));
            }

            int status = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
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
                    batteryStatus = UNKNOWN;
                    break;
            }

            int plugged = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
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
                    plugStatus = UNKNOWN;
                    break;
            }
        }
    }

    @Nullable
    private static Intent getBatteryStatusIntent(Context context) {
        HyperLog.i(TAG, "Retrieving battery value");
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        return context.registerReceiver(null, filter);
    }

    public int getBatteryLevelPercentage() {
        return batteryLevelPercentage;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public String getPlugStatus() {
        return plugStatus;
    }

    public boolean isCharging() {
        return !UNKNOWN.equals(plugStatus);
    }
}
