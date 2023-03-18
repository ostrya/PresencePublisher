package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.log.ui.LogRecyclerViewAdapter;
import org.ostrya.presencepublisher.log.ui.LogType;
import org.ostrya.presencepublisher.log.ui.LogViewModel;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LogFragment extends Fragment {
    private static final String TAG = "LogFragment";
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private void updateLogView(LogRecyclerViewAdapter adapter, LogViewModel viewModel) {
        viewModel.getLogItems().observe(getViewLifecycleOwner(), adapter::submitList);
    }

    private void exportLogs(View view, LogViewModel viewModel) {
        Context context = view.getContext();
        Future<File> fileFuture = viewModel.exportLogs(context);

        executorService.execute(
                () -> {
                    File file = null;
                    try {
                        file = fileFuture.get();
                    } catch (ExecutionException e) {
                        DatabaseLogger.w(TAG, "Unable to export logs", e.getCause());
                    } catch (InterruptedException e) {
                        DatabaseLogger.w(TAG, "Unable to export logs", e);
                    }
                    String message =
                            file != null
                                    ? context.getString(R.string.toast_logs_stored, file)
                                    : context.getString(R.string.toast_logs_not_stored);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
                });
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogViewModel viewModel = new ViewModelProvider(this).get(LogViewModel.class);
        View view = inflater.inflate(R.layout.fragment_log_list, container, false);
        Spinner logTypeSpinner = view.findViewById(R.id.logType);
        RecyclerView recyclerView = view.findViewById(R.id.list);
        Button exportButton = view.findViewById(R.id.export);
        Button clearButton = view.findViewById(R.id.clear);

        if (recyclerView != null) {
            Context context = view.getContext();
            LinearLayoutManager layout = new LinearLayoutManager(context);
            layout.setStackFromEnd(true);
            recyclerView.setLayoutManager(layout);
            LogRecyclerViewAdapter logAdapter = new LogRecyclerViewAdapter();
            recyclerView.setAdapter(logAdapter);
            updateLogView(logAdapter, viewModel);
            if (clearButton != null) {
                clearButton.setOnClickListener(v -> viewModel.clearLogs());
            }
            if (logTypeSpinner != null) {
                ArrayAdapter<CharSequence> typeAdapter =
                        ArrayAdapter.createFromResource(
                                context,
                                LogType.settingDescriptions(),
                                android.R.layout.simple_spinner_item);
                typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                logTypeSpinner.setAdapter(typeAdapter);
                logTypeSpinner.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(
                                    AdapterView<?> parent, View view, int position, long id) {
                                viewModel.setLogType(LogType.settingValues()[position]);
                                updateLogView(logAdapter, viewModel);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // do nothing
                            }
                        });
                // make sure something is selected
                logTypeSpinner.setSelection(0);
            }
        }

        if (exportButton != null) {
            exportButton.setOnClickListener(view1 -> exportLogs(view1, viewModel));
        }
        return view;
    }
}
