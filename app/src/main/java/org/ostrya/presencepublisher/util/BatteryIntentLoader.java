package org.ostrya.presencepublisher.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import androidx.annotation.Nullable;

public class BatteryIntentLoader {
    private final Context applicationContext;

    public BatteryIntentLoader(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Nullable
    public Intent getBatteryIntent() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        return applicationContext.registerReceiver(null, filter);
    }

    public boolean isCharging() {
        Intent batteryIntent = getBatteryIntent();
        if (batteryIntent == null) {
            // if we do not know the charging state, better be safe and assume we are on battery
            return false;
        }
        return batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) > 0;
    }
}
