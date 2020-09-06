package org.ostrya.presencepublisher.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hypertrack.hyperlog.HyperLog;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.ostrya.presencepublisher.R;

import java.util.Collection;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class BeaconScanDialogFragment extends DialogFragment implements BeaconConsumer {
    private static final String TAG = "BeaconScanDialogFragment";
    private static final String REGION_ID = "scan_region_id";
    private BeaconManager beaconManager;
    private ScanCallback scanCallback;
    private Region region;
    private DialogCallback dialogCallback;
    private Set<String> knownBeacons;
    private ProgressBar progressBar;
    private BeaconListAdapter adapter;

    public static BeaconScanDialogFragment getInstance(Context context, DialogCallback dialogCallback, Set<String> knownBeacons) {
        BeaconScanDialogFragment fragment = new BeaconScanDialogFragment();
        fragment.setScanCallback(fragment.new ScanCallback());
        fragment.setRegion(new Region(REGION_ID, null, null, null));
        fragment.setBeaconManager(BeaconManager.getInstanceForApplication(context.getApplicationContext()));
        fragment.setDialogCallback(dialogCallback);
        fragment.setKnownBeacons(knownBeacons);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        ViewGroup root = requireActivity().findViewById(android.R.id.content);
        View view = inflater.inflate(R.layout.dialog_beacon_scan, root, false);

        progressBar = view.findViewById(R.id.beaconScanProgress);
        progressBar.setIndeterminate(true);
        adapter = new BeaconListAdapter(this::onSelect, knownBeacons);
        RecyclerView recyclerView = view.findViewById(R.id.beaconScanList);
        Context context = view.getContext();
        LinearLayoutManager layout = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layout);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(false);
        recyclerView.setItemAnimator(null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.dialog_scan_beacons_title)
                .setView(view)
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                    stopScan();
                    dialogCallback.accept(null);
                });
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopScan();
        beaconManager.unbind(this);
    }

    private void onSelect(Beacon beacon) {
        dialogCallback.accept(beacon);
        dismiss();
    }

    private void setBeaconManager(BeaconManager beaconManager) {
        this.beaconManager = beaconManager;
        beaconManager.bind(this);
    }

    public void setScanCallback(ScanCallback scanCallback) {
        this.scanCallback = scanCallback;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    private void setDialogCallback(DialogCallback dialogCallback) {
        this.dialogCallback = dialogCallback;
    }

    private void setKnownBeacons(Set<String> knownBeacons) {
        this.knownBeacons = knownBeacons;
    }

    private void stopScan() {
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            HyperLog.e(TAG, "Unable to stop scanning for beacons", e);
        }
        beaconManager.setBackgroundMode(true);
        beaconManager.removeRangeNotifier(scanCallback);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            HyperLog.i(TAG, "Starting to scan for beacons");
            beaconManager.setBackgroundMode(false);
            beaconManager.addRangeNotifier(scanCallback);
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            HyperLog.e(TAG, "Unable to start scanning for beacons", e);
            Toast.makeText(requireContext(), R.string.toast_scan_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Context getApplicationContext() {
        return requireContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        HyperLog.i(TAG, "Unbinding from BeaconManager service");
        requireActivity().unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        HyperLog.i(TAG, "Binding to BeaconManager service");
        return requireActivity().bindService(intent, serviceConnection, i);
    }

    public interface DialogCallback {
        void accept(@Nullable Beacon beacon);
    }

    private class ScanCallback implements RangeNotifier {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            HyperLog.v(TAG, "Got callback from beacon scan for region " + region);
            for (Beacon beacon : beacons) {
                if (beacon != null && beacon.getBluetoothAddress() != null) {
                    HyperLog.d(TAG, "Found beacon " + beacon);
                    adapter.addBeacon(fixEmptyName(beacon));
                } else {
                    HyperLog.w(TAG, "Beacon " + beacon + " does not have a Bluetooth address");
                }
            }
        }

        private Beacon fixEmptyName(Beacon beacon) {
            if (beacon.getBluetoothName() != null) {
                return beacon;
            }
            return new Beacon.Builder()
                    .copyBeaconFields(beacon)
                    .setBluetoothName(beacon.getBluetoothAddress())
                    .build();
        }
    }
}
