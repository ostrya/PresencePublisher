package org.ostrya.presencepublisher.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.hypertrack.hyperlog.DeviceLogModel;
import org.ostrya.presencepublisher.R;

public class LogRecyclerViewAdapter extends ListAdapter<DeviceLogModel, LogRecyclerViewAdapter.ViewHolder> {
    private static final DiffUtil.ItemCallback<DeviceLogModel> CALLBACK = new DiffUtil.ItemCallback<DeviceLogModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull DeviceLogModel oldItem, @NonNull DeviceLogModel newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DeviceLogModel oldItem, @NonNull DeviceLogModel newItem) {
            return oldItem.getDeviceLog().equals(newItem.getDeviceLog());
        }
    };

    LogRecyclerViewAdapter() {
        super(CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        DeviceLogModel item = getItem(position);
        holder.item = item;
        holder.contentView.setText(item.getDeviceLog());
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView contentView;
        DeviceLogModel item;

        ViewHolder(View view) {
            super(view);
            contentView = itemView.findViewById(R.id.content);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }
}
