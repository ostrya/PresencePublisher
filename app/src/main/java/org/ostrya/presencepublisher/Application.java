package org.ostrya.presencepublisher;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.log.CustomLogFormat;
import org.ostrya.presencepublisher.log.LogUncaughtExceptionHandler;
import org.ostrya.presencepublisher.receiver.SystemBroadcastReceiver;
import org.ostrya.presencepublisher.ui.notification.NotificationFactory;

public class Application extends android.app.Application {
    public static final int PERMISSION_REQUEST_CODE = 1;
    public static final int LOCATION_REQUEST_CODE = 2;
    public static final int BATTERY_OPTIMIZATION_REQUEST_CODE = 3;
    public static final int ALARM_REQUEST_CODE = 4;
    public static final int MAIN_ACTIVITY_REQUEST_CODE = 5;

    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();
        initNetworkReceiver();
        NotificationFactory.createNotificationChannel(this);
    }

    private void initLogger() {
        HyperLog.initialize(this, new CustomLogFormat(this));
        if (BuildConfig.DEBUG) {
            HyperLog.setLogLevel(Log.VERBOSE);
        } else {
            HyperLog.setLogLevel(Log.INFO);
        }
        Thread.setDefaultUncaughtExceptionHandler(
                new LogUncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()));
    }

    @SuppressWarnings("deprecation")
    private void initNetworkReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SystemBroadcastReceiver receiver = new SystemBroadcastReceiver();
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(receiver, filter);
        }
    }
}
