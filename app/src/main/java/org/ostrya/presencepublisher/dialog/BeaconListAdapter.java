package org.ostrya.presencepublisher.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.mqtt.context.condition.beacon.PresenceBeacon;

import java.util.Set;

public class BeaconListAdapter extends RecyclerView.Adapter<BeaconListAdapter.BeaconHolder> {
    private final SortedList.Callback<PresenceBeacon> beaconBatchedCallback =
            new ListCallback(this);
    private final SortedList<PresenceBeacon> foundBeacons =
            new SortedList<>(PresenceBeacon.class, beaconBatchedCallback);
    private final BeaconScanDialogFragment.DialogCallback dialogCallback;
    private final Set<String> knownBeacons;

    BeaconListAdapter(
            BeaconScanDialogFragment.DialogCallback dialogCallback, Set<String> knownBeacons) {
        this.dialogCallback = dialogCallback;
        this.knownBeacons = knownBeacons;
    }

    @NonNull
    @Override
    public BeaconHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_beacon, parent, false);
        return new BeaconHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BeaconHolder holder, int position) {
        PresenceBeacon beacon = foundBeacons.get(position);
        holder.setBeacon(beacon, !knownBeacons.contains(beacon.toBeaconId()));
        holder.setClickHandler(this::onClick);
    }

    @Override
    public int getItemCount() {
        return foundBeacons.size();
    }

    public void addBeacon(PresenceBeacon beacon) {
        foundBeacons.add(beacon);
    }

    private void onClick(BeaconHolder beaconHolder) {
        dialogCallback.accept(beaconHolder.item);
    }

    interface ClickListener {
        void onClick(BeaconHolder beaconHolder);
    }

    static final class BeaconHolder extends RecyclerView.ViewHolder {
        final TextView beaconType;
        final TextView beaconName;
        final TextView beaconStrength;
        final TextView beaconDistance;
        final TextView beaconAddress;
        PresenceBeacon item;

        public BeaconHolder(@NonNull View view) {
            super(view);
            beaconType = itemView.findViewById(R.id.beaconType);
            beaconName = itemView.findViewById(R.id.beaconName);
            beaconStrength = itemView.findViewById(R.id.beaconStrength);
            beaconDistance = itemView.findViewById(R.id.beaconDistance);
            beaconAddress = itemView.findViewById(R.id.beaconAddress);
        }

        void setBeacon(PresenceBeacon beacon, boolean enabled) {
            item = beacon;
            beaconType.setText(item.getType());
            beaconName.setText(item.getName());
            beaconStrength.setText(
                    String.format(beaconStrength.getTextLocale(), "%d dBm", item.getRssi()));
            beaconDistance.setText(
                    String.format(beaconDistance.getTextLocale(), "%.1f m", item.getDistance()));
            beaconAddress.setText(item.getAddress());
            itemView.setEnabled(enabled);
        }

        void setClickHandler(ClickListener clickListener) {
            itemView.setOnClickListener(view -> clickListener.onClick(this));
        }
    }

    private static final class ListCallback extends SortedListAdapterCallback<PresenceBeacon> {
        public ListCallback(RecyclerView.Adapter adapter) {
            super(adapter);
        }

        @Override
        public int compare(PresenceBeacon item1, PresenceBeacon item2) {
            int result = item1.getAddress().compareTo(item2.getAddress());
            if (result == 0) {
                result = item1.getType().compareTo(item2.getType());
            }
            return result;
        }

        @Override
        public boolean areContentsTheSame(PresenceBeacon oldItem, PresenceBeacon newItem) {
            return areItemsTheSame(oldItem, newItem)
                    && oldItem.getName().equals(newItem.getName())
                    && oldItem.getRssi() == newItem.getRssi()
                    && oldItem.getDistance() == newItem.getDistance();
        }

        @Override
        public boolean areItemsTheSame(PresenceBeacon item1, PresenceBeacon item2) {
            return item1.getAddress().equals(item2.getAddress())
                    && item1.getType().equals(item2.getType());
        }
    }
}
