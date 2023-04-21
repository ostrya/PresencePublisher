package org.ostrya.presencepublisher.preference.condition;

import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;

import static org.ostrya.presencepublisher.dialog.BeaconScanDialogFragment.getInstance;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.BeaconScanDialogFragment;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.context.condition.beacon.PresenceBeacon;
import org.ostrya.presencepublisher.mqtt.context.condition.beacon.PresenceBeaconManager;
import org.ostrya.presencepublisher.preference.common.ClickDummy;

import java.util.Collections;

public class AddBeaconChoicePreferenceDummy extends ClickDummy {
    private static final String TAG = "AddBeaconChoicePreferenceDummy";

    private final ActivityResultLauncher<String> serviceStartLauncher;
    private final ActivityResultLauncher<String[]> permissionRequestLauncher;

    public AddBeaconChoicePreferenceDummy(
            Context context,
            Fragment fragment,
            ActivityResultLauncher<String> serviceStartLauncher,
            ActivityResultLauncher<String[]> permissionRequestLauncher) {
        super(
                context,
                R.drawable.baseline_playlist_add_24,
                R.string.add_beacon_title,
                R.string.add_beacon_summary,
                fragment);
        this.serviceStartLauncher = serviceStartLauncher;
        this.permissionRequestLauncher = permissionRequestLauncher;
    }

    @Override
    protected void onClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && (ContextCompat.checkSelfPermission(
                                        getContext(), Manifest.permission.BLUETOOTH_SCAN)
                                != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(
                                        getContext(), Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED)) {
            permissionRequestLauncher.launch(
                    new String[] {
                        Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
                    });
            return;
        }
        BluetoothManager bluetoothManager =
                (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            DatabaseLogger.w(TAG, "Unable to get bluetooth manager");
        } else {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                serviceStartLauncher.launch(ACTION_REQUEST_ENABLE);
                return;
            }
        }
        BeaconScanDialogFragment instance =
                getInstance(
                        getContext(),
                        this::onScanResult,
                        getSharedPreferences()
                                .getStringSet(
                                        BeaconCategorySupport.BEACON_LIST, Collections.emptySet()));
        instance.show(getParentFragmentManager(), null);
    }

    private void onScanResult(@Nullable PresenceBeacon beacon) {
        if (beacon == null) {
            return;
        }
        PresenceBeaconManager.getInstance().addBeacon(getContext(), beacon);
    }
}
