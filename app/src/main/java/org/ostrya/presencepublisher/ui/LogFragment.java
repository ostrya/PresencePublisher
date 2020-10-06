package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.R;

import java.io.File;

public class LogFragment extends Fragment {

    public LogFragment() {
        // Mandatory empty constructor for the fragment manager to instantiate the
        // fragment (e.g. upon screen orientation changes).
    }

    private static void updateLogView(LogRecyclerViewAdapter adapter) {
        adapter.submitList(HyperLog.getDeviceLogs(false, HyperLog.getDeviceLogBatchCount()));
    }

    private static void exportLogs(View view) {
        Context context = view.getContext();
        if (!HyperLog.hasPendingDeviceLogs()) {
            Toast.makeText(context, R.string.toast_no_logs, Toast.LENGTH_SHORT).show();
            return;
        }
        File logs = HyperLog.getDeviceLogsInFile(context.getApplicationContext(), false);
        Toast.makeText(context, context.getString(R.string.toast_logs_stored, logs), Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.list);
        Button exportButton = view.findViewById(R.id.export);
        Button clearButton = view.findViewById(R.id.clear);
        Button reloadButton = view.findViewById(R.id.reload);

        if (recyclerView != null) {
            Context context = view.getContext();
            LinearLayoutManager layout = new LinearLayoutManager(context);
            layout.setStackFromEnd(true);
            recyclerView.setLayoutManager(layout);
            LogRecyclerViewAdapter adapter = new LogRecyclerViewAdapter();
            recyclerView.setAdapter(adapter);
            updateLogView(adapter);
            if (reloadButton != null) {
                reloadButton.setOnClickListener(v -> updateLogView(adapter));
            }
            if (clearButton != null) {
                clearButton.setOnClickListener(v -> {
                    HyperLog.deleteLogs();
                    updateLogView(adapter);
                });
            }
        }
        if (exportButton != null) {
            exportButton.setOnClickListener(LogFragment::exportLogs);
        }
        return view;
    }
}
