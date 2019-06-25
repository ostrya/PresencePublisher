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
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.ostrya.presencepublisher.ui.ConnectionFragment.*;
import static org.ostrya.presencepublisher.ui.ContentFragment.*;
import static org.ostrya.presencepublisher.ui.ScheduleFragment.*;
import static org.ostrya.presencepublisher.ui.notification.NotificationFactory.getServiceNotification;
import static org.ostrya.presencepublisher.ui.notification.NotificationFactory.updateServiceNotification;

public class ForegroundService extends Service {
    public static final String ALARM_ACTION = "org.ostrya.presencepublisher.ALARM_ACTION";

    private static final String TAG = "ForegroundService";

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
    private AtomicBoolean currentlyRunning = new AtomicBoolean();

    @Override
    public void onCreate() {
        HyperLog.i(TAG, "Starting service");
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
        HyperLog.d(TAG, "Starting service finished");
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        HyperLog.i(TAG, "Received start intent " + (intent == null ? "null" : intent.getAction()));
        start();
        return START_STICKY;
    }

    private void showNotificationAndStartInForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                HyperLog.d(TAG, "Setting notification");
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
                case OFFLINE_PING:
                    HyperLog.i(TAG, "Changed parameter " + key);
                    start();
                    break;
                case LAST_PING:
                case NEXT_PING:
                    break;
                default:
                    HyperLog.v(TAG, "Ignoring unexpected value " + key);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void migrateOldPreference() {
        if (sharedPreferences.contains(SSID) && !sharedPreferences.contains(SSID_LIST)) {
            HyperLog.d(TAG, "Migrating wifi network to new parameter");
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
                    HyperLog.i(TAG, "Network available");
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
        if (!currentlyRunning.compareAndSet(false, true)) {
            HyperLog.d(TAG, "Skip message scheduling as already running");
            return;
        }
        try {
            List<String> messages = getMessagesToSend();
            if (!messages.isEmpty()) {
                HyperLog.d(TAG, "Sending messages in background");
                executorService.submit(() -> doSend(messages));
            }
        } catch (RuntimeException e) {
            HyperLog.w(TAG, "Error while getting messages to send", e);
        }
        int ping = sharedPreferences.getInt(PING, 15);
        nextPing = System.currentTimeMillis() + ping * 60_000L;
        HyperLog.i(TAG, "Re-scheduling for " + new Date(nextPing));
        sharedPreferences.edit().putLong(NEXT_PING, nextPing).apply();
        updateNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextPing, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextPing, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextPing, pendingIntent);
        }
        currentlyRunning.set(false);
    }

    private void doSend(List<String> messages) {
        try {
            mqttService.sendMessages(messages);
            lastPing = System.currentTimeMillis();
            sharedPreferences.edit().putLong(LAST_PING, lastPing).apply();
            updateNotification();
        } catch (Exception e) {
            HyperLog.w(TAG, "Error while sending messages", e);
        }
    }

    private List<String> getMessagesToSend() {
        List<String> content = new ArrayList<>();
        if (isConnectedToWiFi()) {
            String ssid = getSsidIfMatching();
            if (ssid != null) {
                HyperLog.i(TAG, "Scheduling message for SSID " + ssid);
                content.add(sharedPreferences.getString(WIFI_PREFIX + ssid, DEFAULT_CONTENT_ONLINE));
            }
        }
        if (content.isEmpty() && sharedPreferences.getBoolean(OFFLINE_PING, false)) {
            HyperLog.i(TAG, "Scheduling offline message");
            content.add(sharedPreferences.getString(CONTENT_OFFLINE, DEFAULT_CONTENT_OFFLINE));
        }
        return content;
    }

    private boolean isConnectedToWiFi() {
        if (connectivityManager == null) {
            HyperLog.e(TAG, "Connectivity Manager not found");
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
        HyperLog.i(TAG, "Checking SSID");
        String ssid = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (wifiManager == null) {
                HyperLog.e(TAG, "No wifi manager");
            } else {
                ssid = SsidUtil.normalizeSsid(wifiManager.getConnectionInfo().getSSID());
            }
        } else {
            if (connectivityManager == null) {
                HyperLog.e(TAG, "Connectivity Manager not found");
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    ssid = SsidUtil.normalizeSsid(activeNetworkInfo.getExtraInfo());
                }
            }
        }
        if (ssid == null) {
            HyperLog.i(TAG, "No SSID found");
            return null;
        }
        Set<String> targetSsids = sharedPreferences.getStringSet(SSID_LIST, Collections.emptySet());
        if (targetSsids.contains(ssid)) {
            HyperLog.d(TAG, "Correct network found");
            return ssid;
        } else {
            HyperLog.i(TAG, "'" + ssid + "' does not match any desired network '" + targetSsids + "', skipping.");
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
