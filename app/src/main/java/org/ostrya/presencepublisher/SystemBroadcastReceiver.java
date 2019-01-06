package org.ostrya.presencepublisher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.preference.PreferenceManager;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static org.ostrya.presencepublisher.SettingsFragment.AUTOSTART;

public class SystemBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = SystemBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (CONNECTIVITY_ACTION.equals(action)
                || (Intent.ACTION_BOOT_COMPLETED.equals(action) && sharedPreferences.getBoolean(AUTOSTART, false))) {
            Log.i(TAG, "Received intent " + action);
            Intent startIntent = new Intent(context, ForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent);
            } else {
                context.startService(startIntent);
            }
        }
    }
}
