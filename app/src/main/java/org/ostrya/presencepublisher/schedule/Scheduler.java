package org.ostrya.presencepublisher.schedule;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.net.NetworkCapabilities.NET_CAPABILITY_FOREGROUND;
import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED;
import static android.net.NetworkCapabilities.TRANSPORT_CELLULAR;
import static android.net.NetworkCapabilities.TRANSPORT_ETHERNET;
import static android.net.NetworkCapabilities.TRANSPORT_VPN;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;

import static org.ostrya.presencepublisher.PresencePublisher.ALARM_ACTION;
import static org.ostrya.presencepublisher.PresencePublisher.ALARM_PENDING_INTENT_REQUEST_CODE;
import static org.ostrya.presencepublisher.PresencePublisher.NETWORK_PENDING_INTENT_ACTION;
import static org.ostrya.presencepublisher.PresencePublisher.NETWORK_PENDING_INTENT_REQUEST_CODE;
import static org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;
import static org.ostrya.presencepublisher.ui.preference.schedule.ChargingMessageSchedulePreference.CHARGING_MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.schedule.MessageSchedulePreference.MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;
import static org.ostrya.presencepublisher.ui.util.TimestampSummaryProvider.UNDEFINED;
import static org.ostrya.presencepublisher.ui.util.TimestampSummaryProvider.WAITING_FOR_RECONNECT;
import static org.ostrya.presencepublisher.ui.util.TimestampSummaryProvider.getFormattedTimestamp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.BatteryManager;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.receiver.AlarmReceiver;
import org.ostrya.presencepublisher.receiver.ConnectivityBroadcastReceiver;
import org.ostrya.presencepublisher.ui.notification.NotificationFactory;

import java.util.Collections;

public class Scheduler {
    private static final String TAG = "Scheduler";

    private static final int NOTIFICATION_ID = 1;
    public static final long NOW_DELAY = 1_000L;

    private final Context applicationContext;
    private final SharedPreferences sharedPreferences;
    private final AlarmManager alarmManager;
    private final PendingIntent pendingAlarmIntent;
    private final PendingIntent pendingNetworkIntent;

    public Scheduler(Context context) {
        applicationContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(applicationContext, AlarmReceiver.class);
        alarmIntent.setAction(ALARM_ACTION);
        pendingAlarmIntent =
                PendingIntent.getBroadcast(
                        applicationContext,
                        ALARM_PENDING_INTENT_REQUEST_CODE,
                        alarmIntent,
                        FLAG_UPDATE_CURRENT);
        Intent networkIntent = new Intent(applicationContext, ConnectivityBroadcastReceiver.class);
        networkIntent.setAction(NETWORK_PENDING_INTENT_ACTION);
        pendingNetworkIntent =
                PendingIntent.getBroadcast(
                        applicationContext,
                        NETWORK_PENDING_INTENT_REQUEST_CODE,
                        networkIntent,
                        FLAG_UPDATE_CURRENT);
    }

    public void scheduleNow() {
        scheduleFor(System.currentTimeMillis() + NOW_DELAY, false);
    }

    public void scheduleNext() {
        if (isCharging()) {
            int messageScheduleInMinutes = sharedPreferences.getInt(CHARGING_MESSAGE_SCHEDULE, 0);
            if (messageScheduleInMinutes > 0) {
                scheduleFor(System.currentTimeMillis() + messageScheduleInMinutes * 60_000L, true);
                return;
            }
        }
        int messageScheduleInMinutes = sharedPreferences.getInt(MESSAGE_SCHEDULE, 15);
        scheduleFor(
                System.currentTimeMillis() + messageScheduleInMinutes * 60_000L,
                messageScheduleInMinutes < 15);
    }

    public void stopSchedule() {
        alarmManager.cancel(pendingAlarmIntent);
        sharedPreferences.edit().remove(NEXT_SCHEDULE).apply();
        NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ConnectivityManager connectivityManager =
                    applicationContext.getSystemService(ConnectivityManager.class);
            if (connectivityManager != null) {
                connectivityManager.unregisterNetworkCallback(pendingNetworkIntent);
            }
        }
    }

    /**
     * This is the counterpart to the broadcast receiver ConnectivityBroadcastReceiver registered in
     * the manifest for Android 8+, where most implicit broadcasts are no longer delivered.
     */
    public void waitForNetworkReconnect() {
        // for Android 8+, we need to register a callback here, as the ConnectivityBroadcastReceiver
        // registered
        // in the manifest no longer gets any broadcasts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            HyperLog.d(TAG, "Register network callback");
            ConnectivityManager connectivityManager =
                    applicationContext.getSystemService(ConnectivityManager.class);
            if (connectivityManager == null) {
                HyperLog.w(
                        TAG,
                        "Unable to get connectivity manager, re-scheduling even when disconnected");
                scheduleNext();
                return;
            }
            NetworkRequest.Builder requestBuilder =
                    new NetworkRequest.Builder()
                            .addTransportType(TRANSPORT_ETHERNET)
                            .addTransportType(TRANSPORT_VPN)
                            .addTransportType(TRANSPORT_WIFI);
            if (useMobile()) {
                requestBuilder.addTransportType(TRANSPORT_CELLULAR);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                requestBuilder
                        .addCapability(NET_CAPABILITY_NOT_SUSPENDED)
                        .addCapability(NET_CAPABILITY_FOREGROUND);
            }
            NetworkRequest networkRequest = requestBuilder.build();
            connectivityManager.registerNetworkCallback(networkRequest, pendingNetworkIntent);
        }
        sharedPreferences.edit().putLong(NEXT_SCHEDULE, WAITING_FOR_RECONNECT).apply();
        NotificationManagerCompat.from(applicationContext)
                .notify(
                        NOTIFICATION_ID,
                        NotificationFactory.getNotification(
                                applicationContext, getLastSuccess(), WAITING_FOR_RECONNECT));
    }

    private boolean useMobile() {
        return sharedPreferences.getBoolean(SEND_VIA_MOBILE_NETWORK, false)
                && (sharedPreferences.getBoolean(SEND_OFFLINE_MESSAGE, false)
                        || !sharedPreferences
                                .getStringSet(BEACON_LIST, Collections.emptySet())
                                .isEmpty());
    }

    private long getLastSuccess() {
        return sharedPreferences.getLong(LAST_SUCCESS, UNDEFINED);
    }

    private void scheduleFor(long nextSchedule, boolean ignoreBattery) {
        if (!sharedPreferences.getBoolean(LOCATION_CONSENT, false)) {
            HyperLog.w(TAG, "Location consent not given, will not schedule anything.");
            return;
        }
        if (alarmManager == null) {
            HyperLog.e(TAG, "Unable to get alarm manager, cannot schedule!");
            return;
        }
        alarmManager.cancel(pendingAlarmIntent);
        HyperLog.i(TAG, "Next run at " + getFormattedTimestamp(applicationContext, nextSchedule));
        sharedPreferences.edit().putLong(NEXT_SCHEDULE, nextSchedule).apply();
        NotificationManagerCompat.from(applicationContext)
                .notify(
                        NOTIFICATION_ID,
                        NotificationFactory.getNotification(
                                applicationContext, getLastSuccess(), nextSchedule));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ignoreBattery) {
                alarmManager.setAlarmClock(
                        new AlarmManager.AlarmClockInfo(nextSchedule, pendingAlarmIntent),
                        pendingAlarmIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(RTC_WAKEUP, nextSchedule, pendingAlarmIntent);
            }
        } else {
            alarmManager.set(RTC_WAKEUP, nextSchedule, pendingAlarmIntent);
        }
    }

    private boolean isCharging() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = applicationContext.registerReceiver(null, filter);
        if (batteryStatus == null) {
            return false;
        }
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
    }
}
