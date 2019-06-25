package org.ostrya.presencepublisher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.ForegroundService;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        if (ForegroundService.ALARM_ACTION.equals(action)) {
            HyperLog.i(TAG, "Alarm broadcast received");
            intent.setClass(context, ForegroundService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }
}
