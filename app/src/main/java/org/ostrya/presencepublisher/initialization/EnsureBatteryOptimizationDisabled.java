package org.ostrya.presencepublisher.initialization;

import static android.content.Context.POWER_SERVICE;
import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;

import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.ui.contract.IntentActionContract;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Queue;

public class EnsureBatteryOptimizationDisabled extends AbstractChainedHandler<String, Boolean> {
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
        PowerManager powerManager = (PowerManager) activity.getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && powerManager != null
                && !powerManager.isIgnoringBatteryOptimizations(activity.getPackageName())) {
            DatabaseLogger.i(TAG, "Battery optimization not yet disabled, asking user ...");
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            getLauncher().launch(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        }
    }

    @Override
    protected void doHandleResult(Boolean result) {
        if (result) {
            DatabaseLogger.i(TAG, "Battery optimization has successfully been disabled");
            finishInitialization();
        } else {
            DatabaseLogger.w(TAG, "User has cancelled disabling battery optimization");
        }
    }

    @Override
    protected String getName() {
        return "EnsureBatteryOptimizationDisabled";
    }
}
