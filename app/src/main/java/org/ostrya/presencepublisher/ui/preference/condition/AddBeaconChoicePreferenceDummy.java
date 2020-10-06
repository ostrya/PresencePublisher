package org.ostrya.presencepublisher.ui.preference.condition;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.beacon.PresenceBeacon;
import org.ostrya.presencepublisher.beacon.PresenceBeaconManager;
import org.ostrya.presencepublisher.ui.dialog.BeaconScanDialogFragment;

import java.util.Collections;

import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;
import static org.ostrya.presencepublisher.ui.dialog.BeaconScanDialogFragment.getInstance;

public class AddBeaconChoicePreferenceDummy extends Preference {
    public static final String BEACON_LIST = "beacons";
    private static final String TAG = "AddBeaconChoicePreferenceDummy";
    private static final String DUMMY = "beaconChoiceDummy";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AddBeaconChoicePreferenceDummy(Context context, Fragment fragment, ActivityResultLauncher<String> intentLauncher) {
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
                    intentLauncher.launch(ACTION_REQUEST_ENABLE);
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
    private void onScanResult(@Nullable PresenceBeacon beacon) {
        if (beacon == null) {
            return;
        }
        PresenceBeaconManager.getInstance().addBeacon(getContext(), beacon);
    }
}
