package org.ostrya.presencepublisher.ui.preference.condition;

import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;

import static org.ostrya.presencepublisher.ui.dialog.BeaconScanDialogFragment.getInstance;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_LIST;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.beacon.PresenceBeacon;
import org.ostrya.presencepublisher.beacon.PresenceBeaconManager;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.ui.dialog.BeaconScanDialogFragment;
import org.ostrya.presencepublisher.ui.preference.common.ClickDummy;

import java.util.Collections;

public class AddBeaconChoicePreferenceDummy extends ClickDummy {
    private static final String TAG = "AddBeaconChoicePreferenceDummy";

    private final ActivityResultLauncher<String> intentLauncher;

    public AddBeaconChoicePreferenceDummy(
            Context context, Fragment fragment, ActivityResultLauncher<String> intentLauncher) {
        super(
                context,
                R.drawable.baseline_playlist_add_24,
                R.string.add_beacon_title,
                R.string.add_beacon_summary,
                fragment);
        this.intentLauncher = intentLauncher;
    }

    @Override
    protected void onClick() {
        BluetoothManager bluetoothManager =
                (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            DatabaseLogger.w(TAG, "Unable to get bluetooth manager");
        } else {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                intentLauncher.launch(ACTION_REQUEST_ENABLE);
                return;
            }
        }
        BeaconScanDialogFragment instance =
                getInstance(
                        getContext(),
                        this::onScanResult,
                        getSharedPreferences().getStringSet(BEACON_LIST, Collections.emptySet()));
        instance.show(getParentFragmentManager(), null);
    }

    private void onScanResult(@Nullable PresenceBeacon beacon) {
        if (beacon == null) {
            return;
        }
        PresenceBeaconManager.getInstance().addBeacon(getContext(), beacon);
    }
}
