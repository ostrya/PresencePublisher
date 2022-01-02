package org.ostrya.presencepublisher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.ostrya.presencepublisher.PresencePublisher;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.schedule.Scheduler;

public class ConnectivityBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectivityBroadcastReceiver";

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.isConnected()) {
                DatabaseLogger.i(TAG, "Reacting to network change");
                new Scheduler(context).scheduleNow();
            }
        } else if (PresencePublisher.NETWORK_PENDING_INTENT_ACTION.equals(action)) {
            DatabaseLogger.i(TAG, "Reacting to network change");
            new Scheduler(context).scheduleNow();
        }
    }
}
