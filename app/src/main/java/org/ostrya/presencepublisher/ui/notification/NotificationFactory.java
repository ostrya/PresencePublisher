package org.ostrya.presencepublisher.ui.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;

import java.text.DateFormat;
import java.util.Date;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static org.ostrya.presencepublisher.Application.MAIN_ACTIVITY_REQUEST_CODE;

public class NotificationFactory {

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel channel
                        = new NotificationChannel(context.getPackageName(), context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    public static Notification getNotification(Context context, long lastSuccess, long nextSchedule) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent
                = PendingIntent.getActivity(context, MAIN_ACTIVITY_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getPackageName())
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(getLastPing(context, lastSuccess))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(getLastPing(context, lastSuccess))
                        .addLine(getNextPing(context, nextSchedule)))
                .setOnlyAlertOnce(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = builder
                    .setCategory(Notification.CATEGORY_STATUS)
                    .build();
        } else {
            notification = builder
                    .build();
        }
        return notification;
    }

    private static String getLastPing(Context context, long lastSuccess) {
        return String.format(context.getString(R.string.notification_last_ping), getFormattedTimestamp(context, lastSuccess));
    }

    private static String getNextPing(Context context, long nextSchedule) {
        return String.format(context.getString(R.string.notification_next_ping), getFormattedTimestamp(context, nextSchedule));
    }

    private static String getFormattedTimestamp(Context context, long timestamp) {
        if (timestamp == 0L) {
            return context.getString(R.string.value_undefined);
        }
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(timestamp));
    }

}
