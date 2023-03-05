package org.ostrya.presencepublisher.ui.preference.schedule;

import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.schedule.Scheduler;
import org.ostrya.presencepublisher.ui.notification.NotificationFactory;
import org.ostrya.presencepublisher.ui.preference.common.ClickDummy;

public class RunNowPreferenceDummy extends ClickDummy {
    private final NotificationFactory notificationFactory;
    private final Runnable runnable;

    public RunNowPreferenceDummy(Context context, Fragment fragment) {
        super(
                context,
                android.R.drawable.ic_media_play,
                R.string.run_now_title,
                R.string.run_now_summary,
                fragment);
        this.notificationFactory = new NotificationFactory(context.getApplicationContext());
        this.runnable =
                notificationFactory.checkNotificationPermissionThenRunCallback(
                        fragment, this::runNow);
    }

    @Override
    protected void onClick() {
        runnable.run();
    }

    private void runNow(Boolean ignored) {
        Toast.makeText(getContext(), R.string.run_now_toast, Toast.LENGTH_SHORT).show();
        new Scheduler(getContext()).runNow();
    }
}
