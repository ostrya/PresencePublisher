package org.ostrya.presencepublisher;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import org.ostrya.presencepublisher.mqtt.MqttService;
import org.ostrya.presencepublisher.receiver.AlarmReceiver;
import org.ostrya.presencepublisher.util.SsidUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.ostrya.presencepublisher.ui.ConnectionFragment.*;
import static org.ostrya.presencepublisher.ui.ContentFragment.*;
import static org.ostrya.presencepublisher.ui.ScheduleFragment.*;
import static org.ostrya.presencepublisher.ui.notification.NotificationFactory.getServiceNotification;
import static org.ostrya.presencepublisher.ui.notification.NotificationFactory.updateServiceNotification;

public class ForegroundService extends Service {
    public static final String ALARM_ACTION = "org.ostrya.presencepublisher.ALARM_ACTION";

    private static final String TAG = ForegroundService.class.getSimpleName();

    private static final String CHANNEL_ID = "org.ostrya.presencepublisher";
    private static final String CHANNEL_NAME = "Presence Publisher";
    private static final int NOTIFICATION_ID = 1;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private MqttService mqttService;
    private long lastPing;
    private long nextPing;
    private ConnectivityManager connectivityManager;
    private AlarmManager alarmManager;
    private WifiManager wifiManager;
    private SharedPreferences sharedPreferences;
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        Log.d(TAG, "Starting service");
        super.onCreate();
        showNotificationAndStartInForeground();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mqttService = new MqttService(getApplicationContext(), sharedPreferences);
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        alarmManager = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        Intent intent = new Intent(ALARM_ACTION);
        intent.setClass(getApplicationContext(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        initializeParameters();
        migrateOldPreference();
        registerNetworkCallback();
        registerWatchDog();
        Log.d(TAG, "Starting service finished");
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "Received start intent " + (intent == null ? "null" : intent.getAction()));
        start();
        return START_STICKY;
    }

    private void showNotificationAndStartInForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                Log.d(TAG, "Setting notification");
                notificationManager
                        .createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT));
            }
        }

        Notification notification = getServiceNotification(getApplicationContext(), CHANNEL_ID);
        startForeground(NOTIFICATION_ID, notification);
    }

    private void initializeParameters() {
        lastPing = sharedPreferences.getLong(LAST_PING, 0L);
        nextPing = sharedPreferences.getLong(NEXT_PING, 0L);
        sharedPreferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
            Log.d(TAG, "Changed parameter " + key);
            switch (key) {
                case HOST:
                case PORT:
                case LOGIN:
                case PASSWORD:
                case TLS:
                case CLIENT_CERT:
                case TOPIC:
                case SSID_LIST:
                case PING:
                    start();
                    break;
                case LAST_PING:
                case NEXT_PING:
                    break;
                default:
                    Log.v(TAG, "Ignoring unexpected value " + key);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void migrateOldPreference() {
        if (sharedPreferences.contains(SSID) && !sharedPreferences.contains(SSID_LIST)) {
            Log.d(TAG, "Migrating wifi network to new parameter");
            String ssid = sharedPreferences.getString(SSID, "");
            if (!"".equals(ssid)) {
                sharedPreferences.edit().putStringSet(SSID_LIST, Collections.singleton(ssid)).apply();
            }
        }
    }

    private void registerNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(final Network network) {
                    Log.d(TAG, "Network available");
                    super.onAvailable(network);
                    start();
                }
            });
        }
    }

    private void registerWatchDog() {
        // if for some reason individual scheduling fails, this will make sure it is resumed at least once per hour
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR,
                AlarmManager.INTERVAL_HOUR, pendingIntent);
    }

    private void start() {
        try {
            List<String> messages = getMessagesToSend();
            if (!messages.isEmpty()) {
                Log.d(TAG, "Schedule sending messages");
                executorService.submit(() -> doSend(messages));
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Error while getting messages to send", e);
        }
        int ping = sharedPreferences.getInt(PING, 15);
        nextPing = System.currentTimeMillis() + ping * 60_000L;
        Log.d(TAG, "Re-scheduling for " + new Date(nextPing));
        sharedPreferences.edit().putLong(NEXT_PING, nextPing).apply();
        updateNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextPing, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextPing, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextPing, pendingIntent);
        }
    }

    private void doSend(List<String> messages) {
        try {
            mqttService.sendMessages(messages);
            lastPing = System.currentTimeMillis();
            sharedPreferences.edit().putLong(LAST_PING, lastPing).apply();
            updateNotification();
        } catch (Exception e) {
            Log.w(TAG, "Error while sending messages", e);
        }
    }

    private List<String> getMessagesToSend() {
        List<String> content = new ArrayList<>();
        if (isConnectedToWiFi()) {
            String ssid = getSsidIfMatching();
            if (ssid != null) {
                Log.d(TAG, "Correct Wi-Fi connected");
                content.add(sharedPreferences.getString(WIFI_PREFIX + ssid, DEFAULT_CONTENT_ONLINE));
            }
        }
        if (content.isEmpty() && sharedPreferences.getBoolean(OFFLINE_PING, false)) {
            Log.d(TAG, "Not connected to any expected network");
            content.add(sharedPreferences.getString(CONTENT_OFFLINE, DEFAULT_CONTENT_OFFLINE));
        }
        return content;
    }

    private boolean isConnectedToWiFi() {
        if (connectivityManager == null) {
            Log.wtf(TAG, "Connectivity Manager not found");
            return false;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null
                || !activeNetworkInfo.isConnected()) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            for (Network network : connectivityManager.getAllNetworks()) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true;
                }
            }
            return false;
        } else {
            //noinspection deprecation
            return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
    }

    private String getSsidIfMatching() {
        Log.i(TAG, "Checking SSID");
        String ssid = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (wifiManager == null) {
                Log.e(TAG, "No wifi manager");
            } else {
                ssid = SsidUtil.normalizeSsid(wifiManager.getConnectionInfo().getSSID());
            }
        } else {
            if (connectivityManager == null) {
                Log.e(TAG, "Connectivity Manager not found");
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    ssid = SsidUtil.normalizeSsid(activeNetworkInfo.getExtraInfo());
                }
            }
        }
        if (ssid == null) {
            Log.i(TAG, "No SSID found");
            return null;
        }
        Set<String> targetSsids = sharedPreferences.getStringSet(SSID_LIST, Collections.emptySet());
        if (targetSsids.contains(ssid)) {
            Log.i(TAG, "Correct network found");
            return ssid;
        } else {
            Log.d(TAG, "'" + ssid + "' does not match any desired network '" + targetSsids + "', skipping.");
            return null;
        }
    }

    private void updateNotification() {
        NotificationManagerCompat.from(this)
                .notify(NOTIFICATION_ID, updateServiceNotification(getApplicationContext(), lastPing, nextPing, CHANNEL_ID));
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }
}
