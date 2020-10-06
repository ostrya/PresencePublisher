package org.ostrya.presencepublisher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.mqtt.Publisher;

import static org.ostrya.presencepublisher.PresencePublisher.ALARM_ACTION;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ALARM_ACTION.equals(action)) {
            HyperLog.i(TAG, "Alarm broadcast received");
            PendingResult pendingResult = goAsync();
            new Thread(() -> publish(context, pendingResult)).start();
        }
    }

    private void publish(Context context, PendingResult pendingResult) {
        new Publisher(context).publish();
        pendingResult.finish();
    }
}
