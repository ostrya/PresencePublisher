package org.ostrya.presencepublisher.ui.log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.ostrya.presencepublisher.R;

public class LogRecyclerViewAdapter
        extends ListAdapter<LogItem, LogRecyclerViewAdapter.ViewHolder> {
    private static final DiffUtil.ItemCallback<LogItem> CALLBACK =
            new DiffUtil.ItemCallback<LogItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull LogItem oldItem, @NonNull LogItem newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull LogItem oldItem, @NonNull LogItem newItem) {
                    return oldItem.getLine().equals(newItem.getLine());
                }
            };

    public LogRecyclerViewAdapter() {
        super(CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        LogItem item = getItem(position);
        holder.contentView.setText(item.getLine());
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView contentView;

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
