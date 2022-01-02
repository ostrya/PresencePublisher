package org.ostrya.presencepublisher.log;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.ostrya.presencepublisher.log.db.DbLog;
import org.ostrya.presencepublisher.log.db.DbLogDao;

import java.util.List;

public class CleanupWorker extends Worker {
    private static final long SEVEN_DAYS_IN_MILLIS = 7L * 24L * 3600L * 1000L;

    public CleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        long threshold = System.currentTimeMillis() - SEVEN_DAYS_IN_MILLIS;
        cleanLogs(threshold, DatabaseLogger.getInstance().getDetectionLogDao());
        cleanLogs(threshold, DatabaseLogger.getInstance().getMessagesLogDao());
        cleanLogs(threshold, DatabaseLogger.getInstance().getDeveloperLogDao());
        return Result.success();
    }

    private <T extends DbLog> void cleanLogs(long threshold, DbLogDao<T> logDao) {
        List<T> oldEntries = logDao.getEntriesOlderThan(threshold);
        logDao.delete(oldEntries);
    }
}
