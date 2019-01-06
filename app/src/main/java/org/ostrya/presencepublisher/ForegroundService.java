package org.ostrya.presencepublisher;

import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.ostrya.presencepublisher.SettingsFragment.*;

public class ForegroundService extends Service {
    static final String ALARM_ACTION = "org.ostrya.presencepublisher.ALARM_ACTION";

    private static final String TAG = ForegroundService.class.getSimpleName();

    private static final String CHANNEL_ID = "org.ostrya.presencepublisher";
    private static final String CHANNEL_NAME = "Presence Publisher";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String targetSsid;
    private String url;
    private String topic;
    private int ping;
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
        connectivityManager = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        Intent intent = new Intent(ALARM_ACTION);
        intent.setClass(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        initializeParameters();
        registerNetworkCallback();
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
            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                Log.d(TAG, "Setting notification");
                notificationManager
                        .createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT));
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Service is running ...");
        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notification = builder
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
        } else {
            notification = builder
                    .build();
        }
        startForeground(1, notification);
    }

    private void initializeParameters() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        url = sharedPreferences.getString(URL, "tcp://localhost");
        topic = sharedPreferences.getString(TOPIC, "topic");
        targetSsid = sharedPreferences.getString(SSID, "ssid");
        ping = sharedPreferences.getInt(PING, 15);
        sharedPreferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
            Log.d(TAG, "Changed parameter " + key);
            switch (key) {
                case URL:
                    url = prefs.getString(URL, "tcp://localhost");
                    start();
                    break;
                case TOPIC:
                    topic = prefs.getString(TOPIC, "topic");
                    start();
                    break;
                case SSID:
                    targetSsid = prefs.getString(SSID, "ssid");
                    start();
                    break;
                case PING:
                    ping = prefs.getInt(PING, 15);
                    start();
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

    private void start() {
        if (isConnectedToWiFi() && isCorrectSsid()) {
            Log.d(TAG, "Correct Wi-Fi connected");
            executorService.submit(this::sendPing);
        }
        long nextPing = System.currentTimeMillis() + ping * 60_000L;
        sharedPreferences.edit().putLong(NEXT_PING, nextPing).apply();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextPing, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextPing, pendingIntent);
        }
    }

    private boolean isConnectedToWiFi() {
        if (connectivityManager == null) {
            Log.wtf(TAG, "Connectivity Manager not found");
        }
        if (connectivityManager.getActiveNetworkInfo() == null
                || !connectivityManager.getActiveNetworkInfo().isConnected()) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            return connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
        }
    }

    private boolean isCorrectSsid() {
        Log.i(TAG, "Checking SSID");
        String ssid;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (wifiManager == null) {
                Log.wtf(TAG, "No wifi manager");
                return false;
            } else {
                ssid = wifiManager.getConnectionInfo().getSSID();
            }
        } else {
            ssid = connectivityManager.getActiveNetworkInfo().getExtraInfo();
        }
        if (ssid == null) {
            Log.i(TAG, "No SSID found");
        } else {
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            if (ssid.equals(targetSsid)) {
                Log.i(TAG, "Correct network found");
                return true;
            } else {
                Log.d(TAG, ssid + " does not match desired network, skipping.");
            }
        }
        return false;
    }

    private void sendPing() {
        Log.d(TAG, "Try pinging server");
        try {
            MqttClient mqttClient = new MqttClient(url, Settings.Secure.ANDROID_ID, new MemoryPersistence());
            mqttClient.connect();
            mqttClient.publish(topic, "online".getBytes(Charset.forName("UTF-8")), 0, false);
            sharedPreferences.edit().putLong(LAST_PING, System.currentTimeMillis()).apply();
            mqttClient.disconnect();
            mqttClient.close();
            Log.d(TAG, "Ping successful");
        } catch (MqttException e) {
            Log.w(TAG, "Error while sending ping", e);
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

}
