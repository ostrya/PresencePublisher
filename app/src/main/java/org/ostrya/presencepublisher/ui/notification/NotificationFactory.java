package org.ostrya.presencepublisher.ui.notification;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static org.ostrya.presencepublisher.PresencePublisher.NOTIFICATION_REQUEST_CODE;
import static org.ostrya.presencepublisher.ui.util.TimestampSummaryProvider.getFormattedTimestamp;

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

public class NotificationFactory {

    private NotificationFactory() {
        // private constructor for helper class
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel channel =
                        new NotificationChannel(
                                context.getPackageName(),
                                context.getString(R.string.app_name),
                                NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static Notification getNotification(
            Context context, long lastSuccess, long nextSchedule) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent =
                    PendingIntent.getActivity(
                            context,
                            NOTIFICATION_REQUEST_CODE,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            //noinspection UnspecifiedImmutableFlag
            pendingIntent =
                    PendingIntent.getActivity(
                            context,
                            NOTIFICATION_REQUEST_CODE,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification notification;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, context.getPackageName())
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(getLastSuccess(context, lastSuccess))
                        .setContentIntent(pendingIntent)
                        .setStyle(
                                new NotificationCompat.InboxStyle()
                                        .addLine(getLastSuccess(context, lastSuccess))
                                        .addLine(getNextSchedule(context, nextSchedule)))
                        .setOnlyAlertOnce(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = builder.setCategory(Notification.CATEGORY_STATUS).build();
        } else {
            notification = builder.build();
        }
        return notification;
    }

    private static String getLastSuccess(Context context, long lastSuccess) {
        return context.getString(
                R.string.notification_last_success, getFormattedTimestamp(context, lastSuccess));
    }

    private static String getNextSchedule(Context context, long nextSchedule) {
        return context.getString(
                R.string.notification_next_schedule, getFormattedTimestamp(context, nextSchedule));
    }
}
