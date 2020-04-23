package org.ostrya.presencepublisher.ui.dialog;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.service.scanner.CycledLeScanCallback;
import org.altbeacon.beacon.service.scanner.CycledLeScanner;
import org.ostrya.presencepublisher.R;

import java.util.List;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class BeaconScanDialogFragment extends DialogFragment {
    private static final long SCAN_TIME_MILLIS = 20_000;

    private CycledLeScanner leScanner;
    private List<BeaconParser> beaconParsers;
    private DialogCallback dialogCallback;
    private Set<String> knownBeacons;
    private ProgressBar progressBar;
    private BeaconListAdapter adapter;

    public static BeaconScanDialogFragment getInstance(Context context, DialogCallback dialogCallback, Set<String> knownBeacons) {
        BeaconScanDialogFragment fragment = new BeaconScanDialogFragment();
        fragment.setLeScanner(CycledLeScanner.createScanner(context, SCAN_TIME_MILLIS, 0L, false,
                fragment.new ScanCallback(), null));
        fragment.setBeaconParsers(BeaconManager.getInstanceForApplication(context.getApplicationContext()).getBeaconParsers());
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
                    leScanner.stop();
                    dialogCallback.accept(null);
                });
        return builder.create();
    }

    private void onSelect(Beacon beacon) {
        dialogCallback.accept(beacon);
        dismiss();
    }

    private void setLeScanner(CycledLeScanner leScanner) {
        this.leScanner = leScanner;
        this.leScanner.start();
    }

    public void setBeaconParsers(List<BeaconParser> beaconParsers) {
        this.beaconParsers = beaconParsers;
    }

    private void setDialogCallback(DialogCallback dialogCallback) {
        this.dialogCallback = dialogCallback;
    }

    private void setKnownBeacons(Set<String> knownBeacons) {
        this.knownBeacons = knownBeacons;
    }

    public interface DialogCallback {
        void accept(@Nullable Beacon beacon);
    }

    private class ScanCallback implements CycledLeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord, long timestamp) {
            Beacon beacon = null;
            for (BeaconParser parser : beaconParsers) {
                beacon = parser.fromScanData(scanRecord, rssi, bluetoothDevice, timestamp);
                if (beacon != null) {
                    break;
                }
            }
            if (beacon != null && beacon.getBluetoothName() != null && beacon.getBluetoothAddress() != null) {
                adapter.addBeacon(beacon);
            }
        }

        @Override
        public void onCycleEnd() {
            leScanner.stop();
            progressBar.setVisibility(View.GONE);
        }
    }
}
