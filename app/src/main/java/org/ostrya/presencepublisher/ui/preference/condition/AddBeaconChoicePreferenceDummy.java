package org.ostrya.presencepublisher.ui.preference.condition;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import com.hypertrack.hyperlog.HyperLog;
import org.altbeacon.beacon.Beacon;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.beacon.BeaconManager;
import org.ostrya.presencepublisher.ui.dialog.BeaconScanDialogFragment;

import java.util.Collections;

import static org.ostrya.presencepublisher.Application.ON_DEMAND_BLUETOOTH_REQUEST_CODE;
import static org.ostrya.presencepublisher.ui.dialog.BeaconScanDialogFragment.getInstance;

public class AddBeaconChoicePreferenceDummy extends Preference {
    public static final String BEACON_LIST = "beacons";
    private static final String TAG = "AddBeaconChoicePreferenceDummy";
    private static final String DUMMY = "beaconChoiceDummy";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AddBeaconChoicePreferenceDummy(Context context, Fragment fragment) {
        super(context);
        setKey(DUMMY);
        setIcon(android.R.drawable.ic_menu_add);
        setTitle(R.string.add_beacon_title);
        setSummary(R.string.add_beacon_summary);
        setOnPreferenceClickListener(prefs -> {
            BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                HyperLog.w(TAG, "Unable to get bluetooth manager");
            } else {
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    fragment.startActivityForResult(enableBtIntent, ON_DEMAND_BLUETOOTH_REQUEST_CODE);
                    return true;
                }
            }
            BeaconScanDialogFragment instance = getInstance(getContext(), this::onScanResult,
                    getSharedPreferences().getStringSet(BEACON_LIST, Collections.emptySet()));
            instance.show(fragment.getParentFragmentManager(), null);
            return true;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void onScanResult(@Nullable Beacon beacon) {
        if (beacon == null) {
            return;
        }
        BeaconManager.getInstance().addBeacon(getContext(), beacon);
    }
}
