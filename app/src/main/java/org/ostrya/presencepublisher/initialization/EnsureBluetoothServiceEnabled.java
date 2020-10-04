package org.ostrya.presencepublisher.initialization;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.fragment.app.FragmentManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Queue;

import static org.ostrya.presencepublisher.Application.START_BLUETOOTH_REQUEST_CODE;

public class EnsureBluetoothServiceEnabled extends AbstractChainedHandler {
    protected EnsureBluetoothServiceEnabled(Queue<HandlerFactory> handlerChain) {
        super(START_BLUETOOTH_REQUEST_CODE, handlerChain);
    }

    @Override
    protected void doInitialize(MainActivity context) {
        if (context.isLocationServiceNeeded()
                // make linter happy
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && context.isBluetoothBeaconConfigured()) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                HyperLog.w(TAG, "Unable to get bluetooth manager, continuing initialization anyway");
            } else {
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    HyperLog.d(TAG, "Bluetooth service not yet enabled, asking user ...");
                    FragmentManager fm = context.getSupportFragmentManager();

                    ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(this::onResult,
                            R.string.bluetooth_dialog_title, R.string.bluetooth_dialog_message);
                    fragment.show(fm, null);
                    return;
                }
            }
        }
        finishInitialization(context);
    }

    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            parent.startActivityForResult(enableBtIntent, getRequestCode());
        }
    }

    @Override
    protected void doHandleResult(MainActivity context, int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            HyperLog.i(TAG, "Bluetooth service has successfully been started");
            finishInitialization(context);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            HyperLog.w(TAG, "User has cancelled starting bluetooth service");
        } else {
            HyperLog.e(TAG, "Custom result code " + resultCode);
        }
    }
}
