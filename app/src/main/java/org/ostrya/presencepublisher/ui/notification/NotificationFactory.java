package org.ostrya.presencepublisher.ui.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;

import java.text.DateFormat;
import java.util.Date;

import static org.ostrya.presencepublisher.ui.ScheduleFragment.LAST_PING;
import static org.ostrya.presencepublisher.ui.ScheduleFragment.NEXT_PING;

public class NotificationFactory {

    public static Notification getServiceNotification(final Context context, final String channelId) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(getLastPing(context))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(getLastPing(context))
                        .addLine(getNextPing(context)))
                .setOnlyAlertOnce(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = builder
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
        } else {
            notification = builder
                    .build();
        }
        return notification;
    }

    private static String getLastPing(final Context context) {
        String lastPing = getFormattedTimestamp(context, LAST_PING);
        return String.format(context.getString(R.string.notification_last_ping), lastPing);
    }

    private static String getNextPing(final Context context) {
        String nextPing = getFormattedTimestamp(context, NEXT_PING);
        return String.format(context.getString(R.string.notification_next_ping), nextPing);
    }

    private static String getFormattedTimestamp(final Context context, final String key) {
        long timestamp = PreferenceManager.getDefaultSharedPreferences(context).getLong(key, 0L);
        if (timestamp == 0L) {
            return context.getString(R.string.value_undefined);
        }
        return DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(timestamp));
    }

}
