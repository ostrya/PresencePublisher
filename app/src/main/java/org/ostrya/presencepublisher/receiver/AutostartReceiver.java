package org.ostrya.presencepublisher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.schedule.Scheduler;

import static org.ostrya.presencepublisher.ui.preference.schedule.AutostartPreference.AUTOSTART;

public class AutostartReceiver extends BroadcastReceiver {
    private static final String TAG = "AutostartReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if ((Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action))
                && sharedPreferences.getBoolean(AUTOSTART, false)) {
            HyperLog.i(TAG, "Auto-start app");
            new Scheduler(context).scheduleNow();
        }
    }
}
