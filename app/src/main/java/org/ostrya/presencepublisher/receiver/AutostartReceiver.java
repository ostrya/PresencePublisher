package org.ostrya.presencepublisher.receiver;

import static org.ostrya.presencepublisher.ui.preference.schedule.AutostartPreference.AUTOSTART;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.schedule.Scheduler;

public class AutostartReceiver extends BroadcastReceiver {
    private static final String TAG = "AutostartReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        if ((Intent.ACTION_BOOT_COMPLETED.equals(action)
                        || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action))
                && sharedPreferences.getBoolean(AUTOSTART, false)) {
            DatabaseLogger.i(TAG, "Auto-start app");
            new Scheduler(context).scheduleNow();
        }
    }
}
