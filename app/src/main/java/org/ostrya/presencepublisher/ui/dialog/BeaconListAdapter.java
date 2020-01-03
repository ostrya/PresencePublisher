package org.ostrya.presencepublisher.ui.dialog;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;
import org.altbeacon.beacon.Beacon;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.BeaconIdHelper;

import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class BeaconListAdapter extends RecyclerView.Adapter<BeaconListAdapter.BeaconHolder> {
    private final SortedList.Callback<Beacon> beaconBatchedCallback = new ListCallback(this);
    private final SortedList<Beacon> foundBeacons = new SortedList<>(Beacon.class, beaconBatchedCallback);
    private final BeaconScanDialogFragment.DialogCallback dialogCallback;
    private final Set<String> knownBeacons;

    BeaconListAdapter(BeaconScanDialogFragment.DialogCallback dialogCallback, Set<String> knownBeacons) {
        this.dialogCallback = dialogCallback;
        this.knownBeacons = knownBeacons;
    }

    @NonNull
    @Override
    public BeaconHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_beacon, parent, false);
        return new BeaconHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BeaconHolder holder, int position) {
        Beacon beacon = foundBeacons.get(position);
        holder.setBeacon(beacon, !knownBeacons.contains(BeaconIdHelper.toBeaconId(beacon)));
        holder.setClickHandler(this::onClick);
    }

    @Override
    public int getItemCount() {
        return foundBeacons.size();
    }

    public void addBeacon(Beacon beacon) {
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
        Beacon item;

        public BeaconHolder(@NonNull View view) {
            super(view);
            beaconType = itemView.findViewById(R.id.beaconType);
            beaconName = itemView.findViewById(R.id.beaconName);
            beaconStrength = itemView.findViewById(R.id.beaconStrength);
            beaconDistance = itemView.findViewById(R.id.beaconDistance);
            beaconAddress = itemView.findViewById(R.id.beaconAddress);
        }

        void setBeacon(Beacon beacon, boolean enabled) {
            item = beacon;
            beaconType.setText(item.getParserIdentifier());
            beaconName.setText(item.getBluetoothName());
            beaconStrength.setText(String.format(beaconStrength.getTextLocale(), "%d dBm", item.getRssi()));
            beaconDistance.setText(String.format(beaconDistance.getTextLocale(), "%.1f m", item.getDistance()));
            beaconAddress.setText(item.getBluetoothAddress());
            itemView.setEnabled(enabled);
        }

        void setClickHandler(ClickListener clickListener) {
            itemView.setOnClickListener(view -> clickListener.onClick(this));
        }
    }

    private static final class ListCallback extends SortedListAdapterCallback<Beacon> {
        public ListCallback(RecyclerView.Adapter adapter) {
            super(adapter);
        }

        @Override
        public int compare(Beacon item1, Beacon item2) {
            int result = item1.getBluetoothAddress().compareTo(item2.getBluetoothAddress());
            if (result == 0) {
                result = item1.getParserIdentifier().compareTo(item2.getParserIdentifier());
            }
            return result;
        }

        @Override
        public boolean areContentsTheSame(Beacon oldItem, Beacon newItem) {
            return areItemsTheSame(oldItem, newItem)
                    && oldItem.getBluetoothName().equals(newItem.getBluetoothName())
                    && oldItem.getRssi() == newItem.getRssi()
                    && oldItem.getDistance() == newItem.getDistance();
        }

        @Override
        public boolean areItemsTheSame(Beacon item1, Beacon item2) {
            return item1.getBluetoothAddress().equals(item2.getBluetoothAddress())
                    && item1.getParserIdentifier().equals(item2.getParserIdentifier());
        }
    }
}
