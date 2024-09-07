package org.ostrya.presencepublisher.receiver;

import static org.ostrya.presencepublisher.preference.about.LocationConsentPreference.LOCATION_CONSENT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;

import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.context.condition.network.NetworkService;
import org.ostrya.presencepublisher.mqtt.context.condition.network.WifiEventConsumer;

import java.util.Optional;

public class NetworkReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            Context applicationContext = context.getApplicationContext();
            if (PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    .getBoolean(LOCATION_CONSENT, false)) {
                WifiManager wifiManager =
                        (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);
                if (wifiManager == null) {
                    DatabaseLogger.e(TAG, "No wifi manager found, could not detect network");
                } else {
                    Optional<String> ssid =
                            NetworkService.getSsid(
                                    Optional.ofNullable(wifiManager.getConnectionInfo())
                                            .filter(
                                                    w ->
                                                            w.getSupplicantState()
                                                                    == SupplicantState.COMPLETED));
                    WifiEventConsumer consumer = new WifiEventConsumer(applicationContext);
                    if (ssid.isPresent()) {
                        consumer.wifiConnected(ssid.get());
                    } else {
                        consumer.wifiDisconnected(null);
                    }
                }
            }
        }
    }
}
