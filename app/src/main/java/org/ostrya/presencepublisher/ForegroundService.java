package org.ostrya.presencepublisher;

import android.app.*;
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

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.ostrya.presencepublisher.ui.ConnectionFragment.*;
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
                case SSID:
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
            if (isConnectedToWiFi() && isCorrectSsid()) {
                Log.d(TAG, "Correct Wi-Fi connected");
                executorService.submit(this::doSendOnline);
            } else {
                boolean sendOffline = sharedPreferences.getBoolean(OFFLINE_PING, false);
                if(sendOffline) {
                    Log.d(TAG, "Correct Wi-Fi disconnected");
                    executorService.submit(this::doSendOffline);
                }
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Error while checking WiFi network", e);
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

    private void doSendOnline() {
        try {
            mqttService.sendPing("online");
            lastPing = System.currentTimeMillis();
            sharedPreferences.edit().putLong(LAST_PING, lastPing).apply();
            updateNotification();
        } catch (Exception e) {
            Log.w(TAG, "Error while sending ping", e);
        }
    }

    private void doSendOffline() {
        try {
            mqttService.sendPing("offline");
            lastPing = System.currentTimeMillis();
            sharedPreferences.edit().putLong(LAST_PING, lastPing).apply();
            updateNotification();
        } catch (Exception e) {
            Log.w(TAG, "Error while sending ping", e);
        }
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
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
    }

    private boolean isCorrectSsid() {
        Log.i(TAG, "Checking SSID");
        String ssid = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (wifiManager == null) {
                Log.wtf(TAG, "No wifi manager");
            } else {
                ssid = SsidUtil.normalizeSsid(wifiManager.getConnectionInfo().getSSID());
            }
        } else {
            if (connectivityManager == null) {
                Log.wtf(TAG, "Connectivity Manager not found");
                return false;
            }
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                ssid = SsidUtil.normalizeSsid(activeNetworkInfo.getExtraInfo());
            }
        }
        if (ssid == null) {
            Log.i(TAG, "No SSID found");
        } else {
            String targetSsid = sharedPreferences.getString(SSID, "ssid");
            if (ssid.equals(targetSsid)) {
                Log.i(TAG, "Correct network found");
                return true;
            } else {
                Log.d(TAG, "'" + ssid + "' does not match desired network'" + targetSsid + "', skipping.");
            }
        }
        return false;
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
