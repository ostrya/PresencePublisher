package org.ostrya.presencepublisher.message;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.hypertrack.hyperlog.HyperLog;

public class BatteryLevelProvider {
    private static final String TAG = "BatteryLevelProvider";

    private static final String KEY = MessageItem.BATTERY_LEVEL.getName();

    private static final StringEntry UNKNOWN = new StringEntry(KEY, "-1");

    private final Context applicationContext;

    public BatteryLevelProvider(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public StringEntry getBatteryLevel() {
        HyperLog.i(TAG, "Retrieving battery value");
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = applicationContext.registerReceiver(null, filter);

        if (batteryStatus == null) {
            HyperLog.w(TAG, "No battery status received, returning fallback value -1");
            return UNKNOWN;
        }

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (level == -1 || scale == -1) {
            HyperLog.w(TAG, "Invalid level " + level + " or scale " + scale);
            return UNKNOWN;
        }

        int batteryPct = (int) (level / (0.01f * scale));

        return new StringEntry(KEY, Integer.toString(batteryPct));
    }
}
