package org.ostrya.presencepublisher.initialization;

import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import androidx.fragment.app.FragmentManager;

import org.ostrya.presencepublisher.IntentActionContract;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.Queue;

public class EnsureBluetoothServiceEnabled extends AbstractChainedHandler<String, Boolean> {
    protected EnsureBluetoothServiceEnabled(
            MainActivity activity, Queue<HandlerFactory> handlerChain) {
        super(activity, new IntentActionContract(), handlerChain);
    }

    @Override
    protected void doInitialize() {
        if (activity.isLocationPermissionNeeded() && activity.isBluetoothBeaconConfigured()) {
            BluetoothManager bluetoothManager =
                    (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                DatabaseLogger.w(
                        TAG, "Unable to get bluetooth manager, continuing initialization anyway");
            } else {
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    DatabaseLogger.d(TAG, "Bluetooth service not yet enabled, asking user ...");
                    FragmentManager fm = activity.getSupportFragmentManager();

                    ConfirmationDialogFragment fragment =
                            ConfirmationDialogFragment.getInstance(
                                    this::onResult,
                                    R.string.bluetooth_dialog_title,
                                    R.string.bluetooth_dialog_message);
                    fragment.show(fm, null);
                    return;
                }
            }
        }
        finishInitialization();
    }

    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            getLauncher().launch(ACTION_REQUEST_ENABLE);
        }
    }

    @Override
    protected void doHandleResult(Boolean result) {
        if (result) {
            DatabaseLogger.i(TAG, "Bluetooth service has successfully been started");
            finishInitialization();
        } else {
            DatabaseLogger.w(TAG, "User has cancelled starting bluetooth service");
        }
    }

    @Override
    protected String getName() {
        return "EnsureBluetoothServiceEnabled";
    }
}
