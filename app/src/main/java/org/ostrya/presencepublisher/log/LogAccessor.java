package org.ostrya.presencepublisher.log;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import org.ostrya.presencepublisher.log.db.DbLog;
import org.ostrya.presencepublisher.log.db.LogDao;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LogAccessor<T extends DbLog> implements Closeable {
    private static final String TAG = "DbLogAccessor";
    private final ExecutorService workingExecutor = Executors.newCachedThreadPool();
    private final ExecutorService listeningExecutor = Executors.newCachedThreadPool();
    private final LogDao<T> dao;

    public LogAccessor(LogDao<T> dao) {
        this.dao = dao;
    }

    public LiveData<List<LogItem>> getLogs() {
        return dao.getAllContinuously();
    }

    public Future<?> exportLogs(Context context, Uri targetUri) {
        return workingExecutor.submit(
                () -> {
                    try {
                        List<T> logEntries = dao.getAll().get();
                        writeFile(context, targetUri, logEntries);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e.getCause());
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void writeFile(Context context, Uri targetUri, List<T> lines) {
        try (BufferedWriter out =
                new BufferedWriter(
                        new OutputStreamWriter(
                                context.getContentResolver().openOutputStream(targetUri, "wt"),
                                StandardCharsets.UTF_8))) {
            for (T line : lines) {
                out.append(line.getLine());
                out.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        Future<Integer> future = dao.deleteAll(workingExecutor);
        listeningExecutor.execute(
                () -> {
                    try {
                        future.get();
                    } catch (ExecutionException e) {
                        DatabaseLogger.w(TAG, "Unable to clear log", e.getCause());
                    } catch (Exception e) {
                        DatabaseLogger.w(TAG, "Unable to clear log", e);
                    }
                });
    }

    @Override
    public void close() {
        listeningExecutor.shutdown();
        workingExecutor.shutdown();
    }
}
