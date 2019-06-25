package org.ostrya.presencepublisher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.ForegroundService;

import static org.ostrya.presencepublisher.ui.ScheduleFragment.AUTOSTART;

public class SystemBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SystemBroadcastReceiver";

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)
                || (Intent.ACTION_BOOT_COMPLETED.equals(action) && sharedPreferences.getBoolean(AUTOSTART, false))) {
            HyperLog.i(TAG, "Received intent " + action);
            Intent startIntent = new Intent(context, ForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent);
            } else {
                context.startService(startIntent);
            }
        }
    }
}
