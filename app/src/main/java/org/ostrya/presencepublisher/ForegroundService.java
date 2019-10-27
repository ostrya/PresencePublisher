package org.ostrya.presencepublisher;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.message.Message;
import org.ostrya.presencepublisher.message.battery.BatteryMessageProvider;
import org.ostrya.presencepublisher.message.wifi.WifiMessageProvider;
import org.ostrya.presencepublisher.mqtt.MqttService;
import org.ostrya.presencepublisher.receiver.AlarmReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.ostrya.presencepublisher.ui.ScheduleFragment.SSID;
import static org.ostrya.presencepublisher.ui.notification.NotificationFactory.getServiceNotification;
import static org.ostrya.presencepublisher.ui.notification.NotificationFactory.updateServiceNotification;
import static org.ostrya.presencepublisher.ui.preference.AutostartPreference.AUTOSTART;
import static org.ostrya.presencepublisher.ui.preference.BatteryTopicPreference.BATTERY_TOPIC;
import static org.ostrya.presencepublisher.ui.preference.ClientCertificatePreference.CLIENT_CERTIFICATE;
import static org.ostrya.presencepublisher.ui.preference.HostPreference.HOST;
import static org.ostrya.presencepublisher.ui.preference.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.MessageSchedulePreference.MESSAGE_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.NextScheduleTimestampPreference.NEXT_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.OfflineContentPreference.OFFLINE_CONTENT;
import static org.ostrya.presencepublisher.ui.preference.PasswordPreference.PASSWORD;
import static org.ostrya.presencepublisher.ui.preference.PortPreference.PORT;
import static org.ostrya.presencepublisher.ui.preference.PresenceTopicPreference.PRESENCE_TOPIC;
import static org.ostrya.presencepublisher.ui.preference.SendBatteryMessagePreference.SEND_BATTERY_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.SendViaMobileNetworkPreference.SEND_VIA_MOBILE_NETWORK;
import static org.ostrya.presencepublisher.ui.preference.SsidListPreference.SSID_LIST;
import static org.ostrya.presencepublisher.ui.preference.UseTlsPreference.USE_TLS;
import static org.ostrya.presencepublisher.ui.preference.UsernamePreference.USERNAME;
import static org.ostrya.presencepublisher.ui.preference.WifiContentPreference.WIFI_CONTENT_PREFIX;

public class ForegroundService extends Service {
    public static final String ALARM_ACTION = "org.ostrya.presencepublisher.ALARM_ACTION";

    private static final String TAG = "ForegroundService";

    private static final String CHANNEL_ID = "org.ostrya.presencepublisher";
    private static final String CHANNEL_NAME = "Presence Publisher";
    private static final int NOTIFICATION_ID = 1;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AtomicBoolean currentlyRunning = new AtomicBoolean();
    private PendingIntent pendingIntent;
    private MqttService mqttService;
    private ConnectivityManager connectivityManager;
    private AlarmManager alarmManager;
    private long lastSuccess;
    private SharedPreferences sharedPreferences;
    private long nextSchedule;
    private WifiMessageProvider wifiMessageProvider;
    private BatteryMessageProvider batteryMessageProvider;
    private final OnSharedPreferenceChangeListener sharedPreferenceListener = this::onSharedPreferenceChanged;

    public static void startService(Context context) {
        startService(context, new Intent());
    }

    public static void startService(Context context, Intent intent) {
        HyperLog.d(TAG, "Starting service ...");
        Context applicationContext = context.getApplicationContext();
        intent.setClass(applicationContext, ForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(intent);
        } else {
            applicationContext.startService(intent);
        }
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

    @Override
    public void onCreate() {
        HyperLog.i(TAG, "Starting service");
        super.onCreate();
        showNotificationAndStartInForeground();
        mqttService = new MqttService(this);
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        alarmManager = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(ALARM_ACTION);
        intent.setClass(getApplicationContext(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        lastSuccess = sharedPreferences.getLong(LAST_SUCCESS, 0L);
        nextSchedule = sharedPreferences.getLong(NEXT_SCHEDULE, 0L);
        wifiMessageProvider = new WifiMessageProvider(this);
        batteryMessageProvider = new BatteryMessageProvider(this);
        registerPreferenceCallback();
        migrateOldPreference();
        registerNetworkCallback();
        registerWatchDog();
        HyperLog.d(TAG, "Starting service finished");
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

    private void registerPreferenceCallback() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceListener);
    }

    private void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case HOST:
            case PORT:
            case USE_TLS:
            case CLIENT_CERTIFICATE:
            case PRESENCE_TOPIC:
            case MESSAGE_SCHEDULE:
            case USERNAME:
            case PASSWORD:
            case SSID_LIST:
            case SEND_OFFLINE_MESSAGE:
            case SEND_VIA_MOBILE_NETWORK:
            case SEND_BATTERY_MESSAGE:
            case BATTERY_TOPIC:
            case OFFLINE_CONTENT:
                HyperLog.i(TAG, "Changed parameter " + key);
                start();
                break;
            case AUTOSTART:
            case LAST_SUCCESS:
            case NEXT_SCHEDULE:
                break;
            default:
                if (key.startsWith(WIFI_CONTENT_PREFIX)) {
                    HyperLog.i(TAG, "Changed parameter " + key);
                    start();
                } else {
                    HyperLog.v(TAG, "Ignoring unexpected value " + key);
                }
        }
    }

    private void start() {
        if (!currentlyRunning.compareAndSet(false, true)) {
            HyperLog.d(TAG, "Skip message scheduling as already running");
            return;
        }
        try {
            try {
                List<Message> messages = getMessagesToSend();
                if (!messages.isEmpty()) {
                    HyperLog.d(TAG, "Sending messages in background");
                    executorService.submit(() -> doSend(messages));
                }
            } catch (RuntimeException e) {
                HyperLog.w(TAG, "Error while getting messages to send", e);
            }
            int ping = sharedPreferences.getInt(MESSAGE_SCHEDULE, 15);
            nextSchedule = System.currentTimeMillis() + ping * 60_000L;
            HyperLog.i(TAG, "Re-scheduling for " + new Date(nextSchedule));
            sharedPreferences.edit().putLong(NEXT_SCHEDULE, nextSchedule).apply();
            updateNotification();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextSchedule, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextSchedule, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextSchedule, pendingIntent);
            }
        } finally {
            currentlyRunning.set(false);
        }
    }

    private void updateNotification() {
        NotificationManagerCompat.from(this)
                .notify(NOTIFICATION_ID, updateServiceNotification(getApplicationContext(), lastSuccess, nextSchedule, CHANNEL_ID));
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    private void doSend(List<Message> messages) {
        try {
            mqttService.sendMessages(messages);
            lastSuccess = System.currentTimeMillis();
            sharedPreferences.edit().putLong(LAST_SUCCESS, lastSuccess).apply();
            updateNotification();
        } catch (Exception e) {
            HyperLog.w(TAG, "Error while sending messages", e);
        }
    }

    private List<Message> getMessagesToSend() {
        List<Message> result = new ArrayList<>();
        result.addAll(wifiMessageProvider.getMessages());
        result.addAll(batteryMessageProvider.getMessages());
        return Collections.unmodifiableList(result);
    }
}
