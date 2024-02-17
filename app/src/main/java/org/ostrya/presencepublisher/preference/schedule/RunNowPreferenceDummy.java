package org.ostrya.presencepublisher.preference.schedule;

import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.notification.NotificationFactory;
import org.ostrya.presencepublisher.preference.common.ClickDummy;
import org.ostrya.presencepublisher.schedule.Scheduler;

public class RunNowPreferenceDummy extends ClickDummy {
    private final Runnable runnable;

    public RunNowPreferenceDummy(Context context, Fragment fragment) {
        super(
                context,
                R.drawable.baseline_send_24,
                R.string.run_now_title,
                R.string.run_now_summary,
                fragment);
        NotificationFactory notificationFactory =
                new NotificationFactory(context.getApplicationContext());
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
        new Thread(() -> new Scheduler(getContext()).runNow()).start();
    }
}
