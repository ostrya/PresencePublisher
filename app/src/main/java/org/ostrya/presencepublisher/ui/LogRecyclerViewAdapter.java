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
        holder.mItem = item;
        holder.mContentView.setText(item.getDeviceLog());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mContentView;
        DeviceLogModel mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
