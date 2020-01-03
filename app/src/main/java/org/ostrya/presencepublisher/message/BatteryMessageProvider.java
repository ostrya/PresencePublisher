package org.ostrya.presencepublisher.message;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import com.hypertrack.hyperlog.HyperLog;

import java.util.Collections;
import java.util.List;

import static org.ostrya.presencepublisher.ui.preference.schedule.BatteryTopicPreference.BATTERY_TOPIC;
import static org.ostrya.presencepublisher.ui.preference.schedule.SendBatteryMessagePreference.SEND_BATTERY_MESSAGE;

public class BatteryMessageProvider extends AbstractMessageProvider {
    private static final String TAG = "BatteryMessageProvider";

    public BatteryMessageProvider(Context context) {
        super(context, BATTERY_TOPIC);
    }

    @Override
    protected List<String> getMessageContents() {
        if (!getSharedPreferences().getBoolean(SEND_BATTERY_MESSAGE, false)) {
            HyperLog.d(TAG, "Battery messages disabled, not generating any messages");
            return Collections.emptyList();
        }

        HyperLog.i(TAG, "Scheduling battery message");
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, filter);

        if (batteryStatus == null) {
            HyperLog.w(TAG, "No battery status received, unable to generate message");
            return Collections.emptyList();
        }

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = (int) (level / (0.01f * scale));

        return Collections.singletonList(Integer.toString(batteryPct));
    }
}
