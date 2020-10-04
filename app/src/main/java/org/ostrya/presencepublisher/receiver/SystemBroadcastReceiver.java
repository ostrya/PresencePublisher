package org.ostrya.presencepublisher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.schedule.Scheduler;

import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;
import static org.ostrya.presencepublisher.ui.preference.schedule.AutostartPreference.AUTOSTART;

public class SystemBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SystemBroadcastReceiver";

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            boolean useMobile = sharedPreferences.getBoolean(SEND_OFFLINE_MESSAGE, false)
                    && sharedPreferences.getBoolean(SEND_VIA_MOBILE_NETWORK, false);
            if (networkInfo != null && networkInfo.isConnected()
                    && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                    || networkInfo.getType() == ConnectivityManager.TYPE_VPN
                    || networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
                    || useMobile)) {
                HyperLog.i(TAG, "Reacting to network change");
                new Scheduler(context).scheduleNow();
            }
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action) && sharedPreferences.getBoolean(AUTOSTART, false)) {
            HyperLog.i(TAG, "Starting after boot");
            new Scheduler(context).scheduleNow();
        }
    }
}
