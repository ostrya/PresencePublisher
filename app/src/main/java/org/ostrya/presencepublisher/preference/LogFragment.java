package org.ostrya.presencepublisher.preference;

import android.content.Context;
import android.net.Uri;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.log.ui.LogRecyclerViewAdapter;
import org.ostrya.presencepublisher.log.ui.LogType;
import org.ostrya.presencepublisher.log.ui.LogViewModel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class LogFragment extends Fragment {
    private static final String TAG = "LogFragment";
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ActivityResultLauncher<String> exportLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument("text/plain"), this::onFileSelected);
    private Function<Uri, Future<?>> logFutureFunction;

    private void updateLogView(RecyclerView recyclerView, LogViewModel viewModel) {
        LogRecyclerViewAdapter logAdapter = new LogRecyclerViewAdapter();
        recyclerView.setAdapter(logAdapter);
        viewModel.getLogItems().observe(getViewLifecycleOwner(), logAdapter::submitList);
    }

    private void exportLogs(View view, LogViewModel viewModel) {
        Context context = view.getContext();
        logFutureFunction = (uri) -> viewModel.exportLogs(context, uri);
        exportLauncher.launch(System.currentTimeMillis() + ".log");
    }

    private void onFileSelected(Uri targetUri) {
        Context context = getContext();
        if (targetUri != null && context != null && logFutureFunction != null) {
            executorService.execute(
                    () -> {
                        String message;
                        try {
                            logFutureFunction.apply(targetUri).get();
                            logFutureFunction = null;
                            message = context.getString(R.string.message_logs_stored);
                        } catch (ExecutionException e) {
                            DatabaseLogger.w(TAG, "Unable to export logs", e.getCause());
                            message =
                                    context.getString(
                                            R.string.message_logs_not_stored,
                                            e.getCause().getLocalizedMessage());
                        } catch (InterruptedException e) {
                            DatabaseLogger.w(TAG, "Unable to export logs", e);
                            message =
                                    context.getString(
                                            R.string.message_logs_not_stored,
                                            e.getLocalizedMessage());
                        }
                        final String finalMessage = message;
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(
                                () -> {
                                    AlertDialog.Builder builder =
                                            new AlertDialog.Builder(requireContext());
                                    builder.setTitle(R.string.button_export);
                                    builder.setMessage(finalMessage);
                                    builder.setNeutralButton(R.string.dialog_close, null);
                                    builder.create().show();
                                });
                    });
        }
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
            updateLogView(recyclerView, viewModel);
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
                                updateLogView(recyclerView, viewModel);
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

    @Override
    public void onDestroyView() {
        executorService.shutdown();
        super.onDestroyView();
    }
}
