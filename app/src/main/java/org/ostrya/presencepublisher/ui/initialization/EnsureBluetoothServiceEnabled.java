package org.ostrya.presencepublisher.ui.initialization;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import androidx.fragment.app.FragmentManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.contract.IntentActionContract;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Queue;

import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;

public class EnsureBluetoothServiceEnabled extends AbstractChainedHandler<String, Boolean> {
    protected EnsureBluetoothServiceEnabled(Queue<HandlerFactory> handlerChain) {
        super(new IntentActionContract(), handlerChain);
    }

    @Override
    protected void doInitialize(MainActivity activity) {
        if (activity.isLocationServiceNeeded()
                // make linter happy
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && activity.isBluetoothBeaconConfigured()) {
            BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                HyperLog.w(TAG, "Unable to get bluetooth manager, continuing initialization anyway");
            } else {
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    HyperLog.d(TAG, "Bluetooth service not yet enabled, asking user ...");
                    FragmentManager fm = activity.getSupportFragmentManager();

                    ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(this::onResult,
                            R.string.bluetooth_dialog_title, R.string.bluetooth_dialog_message);
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
            HyperLog.i(TAG, "Bluetooth service has successfully been started");
            finishInitialization();
        } else {
            HyperLog.w(TAG, "User has cancelled starting bluetooth service");
        }
    }

    @Override
    protected String getName() {
        return "EnsureBluetoothServiceEnabled";
    }
}
