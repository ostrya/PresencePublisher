package org.ostrya.presencepublisher.notification;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static org.ostrya.presencepublisher.PresencePublisher.NOTIFICATION_REQUEST_CODE;
import static org.ostrya.presencepublisher.PresencePublisher.STATUS_NOTIFICATION_ID;
import static org.ostrya.presencepublisher.ui.util.TimestampFormatter.format;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;

public class NotificationFactory {
    private static final String PROGRESS_CHANNEL_ID = "progress";
    private static final String STATUS_CHANNEL_ID = "status";
    private final Context applicationContext;
    private final NotificationManagerCompat notificationManager;

    public NotificationFactory(Context applicationContext) {
        this.applicationContext = applicationContext;
        this.notificationManager = NotificationManagerCompat.from(applicationContext);
    }

    public void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null) {
                NotificationChannel progressChannel =
                        new NotificationChannel(
                                PROGRESS_CHANNEL_ID,
                                applicationContext.getString(R.string.progress_channel),
                                NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(progressChannel);
                NotificationChannel statusChannel =
                        new NotificationChannel(
                                STATUS_CHANNEL_ID,
                                applicationContext.getString(R.string.status_channel),
                                NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(statusChannel);
            }
        }
    }

    public Runnable checkNotificationPermissionThenRunCallback(
            Fragment fragment, ActivityResultCallback<Boolean> callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityResultLauncher<String> launcher =
                    fragment.registerForActivityResult(
                            new ActivityResultContracts.RequestPermission(), callback);
            return () -> {
                // looks like this method returns false only if the user never chose before, but
                // returns true even if the user selected "don't allow", so we don't need to
                // remember this ourselves
                if (!notificationManager.areNotificationsEnabled()) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    callback.onActivityResult(true);
                }
            };
        } else {
            return () -> callback.onActivityResult(true);
        }
    }

    public void updateStatusNotification(long lastSuccess, long nextSchedule) {
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(
                    STATUS_NOTIFICATION_ID, getStatusNotification(lastSuccess, nextSchedule));
        }
    }

    public Notification getProgressNotification() {
        Notification notification;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(applicationContext, PROGRESS_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(applicationContext.getString(R.string.app_name))
                        .setContentText(
                                applicationContext.getString(R.string.progress_notification))
                        .setSilent(true)
                        .setOnlyAlertOnce(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_PROGRESS);
        }
        notification = builder.build();

        return notification;
    }

    private Notification getStatusNotification(long lastSuccess, long nextSchedule) {
        Intent intent = new Intent(applicationContext, MainActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent =
                    PendingIntent.getActivity(
                            applicationContext,
                            NOTIFICATION_REQUEST_CODE,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            //noinspection UnspecifiedImmutableFlag
            pendingIntent =
                    PendingIntent.getActivity(
                            applicationContext,
                            NOTIFICATION_REQUEST_CODE,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification notification;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(applicationContext, STATUS_CHANNEL_ID)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(applicationContext.getString(R.string.app_name))
                        .setContentText(getLastSuccess(applicationContext, lastSuccess))
                        .setContentIntent(pendingIntent)
                        .setStyle(
                                new NotificationCompat.InboxStyle()
                                        .addLine(getLastSuccess(applicationContext, lastSuccess))
                                        .addLine(getNextSchedule(applicationContext, nextSchedule)))
                        .setOnlyAlertOnce(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_STATUS);
        }

        notification = builder.build();
        return notification;
    }

    private static String getLastSuccess(Context context, long lastSuccess) {
        return context.getString(R.string.notification_last_success, format(context, lastSuccess));
    }

    private static String getNextSchedule(Context context, long nextSchedule) {
        return context.getString(
                R.string.notification_next_schedule, format(context, nextSchedule));
    }
}
