package org.ostrya.presencepublisher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.Application;
import org.ostrya.presencepublisher.schedule.Scheduler;

import java.util.Collections;

import static org.ostrya.presencepublisher.ui.preference.condition.AddBeaconChoicePreferenceDummy.BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;

public class ConnectivityBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectivityBroadcastReceiver";

    public static boolean useMobile(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(SEND_VIA_MOBILE_NETWORK, false)
                && (sharedPreferences.getBoolean(SEND_OFFLINE_MESSAGE, false)
                || !sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet()).isEmpty());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.isConnected()
                    && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                    || networkInfo.getType() == ConnectivityManager.TYPE_VPN
                    || networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
                    || useMobile(context))) {
                HyperLog.i(TAG, "Reacting to network change");
                new Scheduler(context).scheduleNow();
            }
        } else if (Application.NETWORK_PENDINT_INTENT_ACTION.equals(action)
                && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            HyperLog.i(TAG, "Reacting to network change");
            new Scheduler(context).scheduleNow();
        }
    }
}
