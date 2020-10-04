package org.ostrya.presencepublisher.initialization;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import androidx.fragment.app.FragmentManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Queue;

import static android.content.Context.POWER_SERVICE;
import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
import static org.ostrya.presencepublisher.Application.BATTERY_OPTIMIZATION_REQUEST_CODE;

public class EnsureBatteryOptimizationDisabled extends AbstractChainedHandler {
    protected EnsureBatteryOptimizationDisabled(Queue<HandlerFactory> handlerChain) {
        super(BATTERY_OPTIMIZATION_REQUEST_CODE, handlerChain);
    }

    @Override
    protected void doInitialize(MainActivity context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager != null
                && !powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
            HyperLog.i(TAG, "Battery optimization not yet disabled, asking user ...");
            FragmentManager fm = context.getSupportFragmentManager();

            // this app should fall under "task automation app" in
            // https://developer.android.com/training/monitoring-device-state/doze-standby.html#whitelisting-cases
            @SuppressLint("BatteryLife")
            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(this::onResult,
                    R.string.battery_optimization_dialog_title, R.string.battery_optimization_dialog_message);
            fragment.show(fm, null);
        } else {
            finishInitialization(context);
        }
    }

    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            Uri packageUri = Uri.fromParts("package", parent.getPackageName(), null);
            parent.startActivityForResult(
                    new Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri),
                    getRequestCode());
        }
    }

    @Override
    protected void doHandleResult(MainActivity context, int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            HyperLog.i(TAG, "Battery optimization has successfully been disabled");
            finishInitialization(context);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            HyperLog.w(TAG, "User has cancelled disabling battery optimization");
        } else {
            HyperLog.e(TAG, "Custom result code " + resultCode);
        }
    }
}
