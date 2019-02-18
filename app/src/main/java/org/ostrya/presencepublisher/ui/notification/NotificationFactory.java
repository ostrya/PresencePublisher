package org.ostrya.presencepublisher.ui.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;

import java.text.DateFormat;
import java.util.Date;

public class NotificationFactory {

    public static Notification getServiceNotification(final Context context, final String channelId) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentIntent(pendingIntent)
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

    public static Notification updateServiceNotification(final Context context, final long lastPing, final long nextPing,
                                                         final String channelId) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(getLastPing(context, lastPing))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(getLastPing(context, lastPing))
                        .addLine(getNextPing(context, nextPing)))
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

    private static String getLastPing(final Context context, final long lastPing) {
        return String.format(context.getString(R.string.notification_last_ping), getFormattedTimestamp(context, lastPing));
    }

    private static String getNextPing(final Context context, final long nextPing) {
        return String.format(context.getString(R.string.notification_next_ping), getFormattedTimestamp(context, nextPing));
    }

    private static String getFormattedTimestamp(final Context context, final long timestamp) {
        if (timestamp == 0L) {
            return context.getString(R.string.value_undefined);
        }
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(timestamp));
    }

}
