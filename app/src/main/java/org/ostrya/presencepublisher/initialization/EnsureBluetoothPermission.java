package org.ostrya.presencepublisher.initialization;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.Queue;

public class EnsureBluetoothPermission extends AbstractChainedHandler<String, Boolean> {
    protected EnsureBluetoothPermission(MainActivity activity, Queue<HandlerFactory> handlerChain) {
        super(activity, new ActivityResultContracts.RequestPermission(), handlerChain);
    }

    @Override
    protected void doInitialize() {
        if (activity.isLocationPermissionNeeded()
                && activity.isBluetoothBeaconConfigured()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED) {
            DatabaseLogger.i(TAG, "Bluetooth scan permission not yet granted, asking user ...");
            FragmentManager fm = activity.getSupportFragmentManager();
            ConfirmationDialogFragment fragment =
                    ConfirmationDialogFragment.getInstance(
                            this::onResult,
                            R.string.bluetooth_permission_dialog_title,
                            R.string.bluetooth_permission_dialog_message);
            fragment.show(fm, null);
        } else {
            finishInitialization();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            getLauncher().launch(Manifest.permission.BLUETOOTH_SCAN);
        } else {
            finishInitialization();
        }
    }

    @Override
    protected void doHandleResult(Boolean result) {
        if (result) {
            DatabaseLogger.i(TAG, "Successfully granted bluetooth permission");
        } else {
            DatabaseLogger.w(TAG, "Bluetooth permission not granted, continuing anyway");
        }
        finishInitialization();
    }

    @Override
    protected String getName() {
        return "EnsureBluetoothPermission";
    }
}
