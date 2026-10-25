package org.ostrya.presencepublisher.initialization;

import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.PowerManager;

import androidx.fragment.app.FragmentManager;

import org.ostrya.presencepublisher.IntentActionContract;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.Queue;

class EnsureBatteryOptimizationDisabled extends AbstractChainedHandler<String, Boolean> {
    protected EnsureBatteryOptimizationDisabled(
            MainActivity activity, Queue<HandlerFactory> handlerChain) {
        super(
                activity,
                new IntentActionContract(
                        context -> Uri.fromParts("package", context.getPackageName(), null)),
                handlerChain);
    }

    @Override
    protected void doInitialize() {
        PowerManager powerManager = activity.getSystemService(PowerManager.class);
        if (powerManager != null
                && !powerManager.isIgnoringBatteryOptimizations(activity.getPackageName())) {
            DatabaseLogger.d(getName(), "Battery optimization not yet disabled, asking user ...");
            FragmentManager fm = activity.getSupportFragmentManager();

            // this app should fall under "task automation app" in
            // https://developer.android.com/training/monitoring-device-state/doze-standby.html#whitelisting-cases
            @SuppressLint("BatteryLife")
            ConfirmationDialogFragment fragment =
                    ConfirmationDialogFragment.getInstance(
                            this::onResult,
                            R.string.battery_optimization_dialog_title,
                            R.string.battery_optimization_dialog_message);
            fragment.show(fm, null);
        } else {
            finishInitialization();
        }
    }

    @SuppressLint("BatteryLife")
    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            getLauncher().launch(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        }
    }

    @Override
    protected void doHandleResult(Boolean result) {
        if (Boolean.TRUE.equals(result)) {
            DatabaseLogger.d(getName(), "Battery optimization has successfully been disabled");
            finishInitialization();
        } else {
            DatabaseLogger.w(getName(), "User has cancelled disabling battery optimization");
        }
    }

    @Override
    protected String getName() {
        return "EnsureBatteryOptimizationDisabled";
    }
}
