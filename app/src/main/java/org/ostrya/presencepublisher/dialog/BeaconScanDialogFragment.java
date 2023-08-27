package org.ostrya.presencepublisher.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.context.condition.beacon.PresenceBeacon;

import java.util.Collection;
import java.util.Set;

public class BeaconScanDialogFragment extends DialogFragment {
    private static final String TAG = "BeaconScanDialogFragment";
    private static final String REGION_ID = "scan_region_id";
    private BeaconManager beaconManager;
    private ScanCallback scanCallback;
    private Region region;
    private DialogCallback dialogCallback;
    private Set<String> knownBeacons;
    private ProgressBar progressBar;
    private BeaconListAdapter adapter;

    public static BeaconScanDialogFragment getInstance(
            Context context, DialogCallback dialogCallback, Set<String> knownBeacons) {
        BeaconScanDialogFragment fragment = new BeaconScanDialogFragment();
        fragment.setScanCallback(fragment.new ScanCallback());
        fragment.setRegion(new Region(REGION_ID, null, null, null));
        fragment.setBeaconManager(
                BeaconManager.getInstanceForApplication(context.getApplicationContext()));
        fragment.setDialogCallback(dialogCallback);
        fragment.setKnownBeacons(knownBeacons);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
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

        DatabaseLogger.i(TAG, "Starting to scan for beacons");
        beaconManager.addRangeNotifier(scanCallback);
        beaconManager.startRangingBeacons(region);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.dialog_scan_beacons_title)
                .setView(view)
                .setNegativeButton(
                        R.string.dialog_cancel,
                        (dialog, which) -> {
                            stopScan();
                            dialogCallback.accept(null);
                        });
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopScan();
    }

    private void onSelect(PresenceBeacon beacon) {
        dialogCallback.accept(beacon);
        dismiss();
    }

    private void setBeaconManager(BeaconManager beaconManager) {
        this.beaconManager = beaconManager;
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
        beaconManager.stopRangingBeacons(region);
        beaconManager.removeRangeNotifier(scanCallback);
        progressBar.setVisibility(View.GONE);
    }

    public interface DialogCallback {
        void accept(@Nullable PresenceBeacon beacon);
    }

    private class ScanCallback implements RangeNotifier {
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            DatabaseLogger.v(TAG, "Got callback from beacon scan for region " + region);
            for (Beacon beacon : beacons) {
                if (beacon != null
                        && beacon.getBluetoothAddress() != null
                        && beacon.getParserIdentifier() != null) {
                    DatabaseLogger.d(TAG, "Found beacon " + beacon);
                    adapter.addBeacon(new PresenceBeacon(beacon));
                } else {
                    DatabaseLogger.w(TAG, "Beacon " + beacon + " is incomplete");
                }
            }
        }
    }
}
