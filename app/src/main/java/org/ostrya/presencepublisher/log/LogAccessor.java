package org.ostrya.presencepublisher.log;

import android.content.Context;

import androidx.lifecycle.LiveData;

import org.ostrya.presencepublisher.log.db.DbLog;
import org.ostrya.presencepublisher.log.db.LogDao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LogAccessor<T extends DbLog> {
    private static final String TAG = "DbLogAccessor";
    private final ExecutorService workingExecutor = Executors.newCachedThreadPool();
    private final Executor listeningExecutor = Executors.newCachedThreadPool();
    private final LogDao<T> dao;

    public LogAccessor(LogDao<T> dao) {
        this.dao = dao;
    }

    public LiveData<List<LogItem>> getLogs() {
        return dao.getAllContinuously();
    }

    public Future<File> exportLogs(Context context) {
        return workingExecutor.submit(
                () -> {
                    try {
                        List<T> logEntries = dao.getAll().get();
                        return writeFile(context, logEntries);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e.getCause());
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private File writeFile(Context context, List<T> lines) {
        File parentDir = context.getExternalFilesDir(null);
        if (parentDir == null) {
            throw new RuntimeException("Unable to open external files directory");
        }
        File result = new File(parentDir, System.currentTimeMillis() + ".log");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(result, false))) {
            for (T line : lines) {
                out.append(
                        DatabaseLogger.getFormattedLog(
                                line.getLine(), new Date(line.getTimestamp())));
                out.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
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
}
