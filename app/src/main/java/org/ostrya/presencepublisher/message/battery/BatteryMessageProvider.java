package org.ostrya.presencepublisher.message.battery;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.message.Message;

import java.util.Collections;
import java.util.List;

import static org.ostrya.presencepublisher.ui.preference.BatteryTopicPreference.BATTERY_TOPIC;
import static org.ostrya.presencepublisher.ui.preference.SendBatteryMessagePreference.SEND_BATTERY_MESSAGE;

public class BatteryMessageProvider {
    private static final String TAG = "BatteryMessageProvider";

    private final Context applicationContext;
    private final SharedPreferences sharedPreferences;

    public BatteryMessageProvider(Context context) {
        this.applicationContext = context.getApplicationContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    }

    public List<Message> getMessages() {
        if (!sharedPreferences.getBoolean(SEND_BATTERY_MESSAGE, false)) {
            HyperLog.d(TAG, "Battery messages disabled, not generating any messages");
            return Collections.emptyList();
        }
        String topic = sharedPreferences.getString(BATTERY_TOPIC, null);
        if (topic == null) {
            HyperLog.w(TAG, "No topic defined, not generating any messages");
            return Collections.emptyList();
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = applicationContext.registerReceiver(null, filter);

        if (batteryStatus == null) {
            HyperLog.w(TAG, "No battery status received, unable to generate message");
            return Collections.emptyList();
        }

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = (int) (level / (0.01f * scale));

        return Collections.singletonList(Message.messageForTopic(topic)
                .withContent(Integer.toString(batteryPct)));
    }
}
