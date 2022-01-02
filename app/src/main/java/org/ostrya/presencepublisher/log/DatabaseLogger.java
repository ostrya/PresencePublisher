package org.ostrya.presencepublisher.log;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;

import org.ostrya.presencepublisher.log.db.DbLog;
import org.ostrya.presencepublisher.log.db.DbLogDao;
import org.ostrya.presencepublisher.log.db.DetectionLog;
import org.ostrya.presencepublisher.log.db.DetectionLogDao;
import org.ostrya.presencepublisher.log.db.DeveloperLog;
import org.ostrya.presencepublisher.log.db.DeveloperLogDao;
import org.ostrya.presencepublisher.log.db.LogDatabase;
import org.ostrya.presencepublisher.log.db.MessagesLog;
import org.ostrya.presencepublisher.log.db.MessagesLogDao;
import org.ostrya.presencepublisher.message.Message;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DatabaseLogger {
    private static final String TAG = "DatabaseLogger";
    private static final String MESSAGE_FORMAT = "%tF %<tT: %s";
    private static final String MESSAGE_LOG_FORMAT = "Sent to topic '%s':%n%s";
    private static final String DEVELOPER_LOG_FORMAT = "[%s/%s] %s";
    private static DatabaseLogger instance = null;

    private final int logLevel;
    private final Executor executor = Executors.newCachedThreadPool();
    private final DetectionLogDao detectionLogDao;
    private final MessagesLogDao messagesLogDao;
    private final DeveloperLogDao developerLogDao;

    public DatabaseLogger(Context applicationContext, int logLevel) {
        this.logLevel = logLevel;
        LogDatabase logDatabase =
                Room.databaseBuilder(applicationContext, LogDatabase.class, "log-database").build();
        detectionLogDao = logDatabase.detectionLogDao();
        messagesLogDao = logDatabase.messagesLogDao();
        developerLogDao = logDatabase.developerLogDao();
        WorkManager.getInstance(applicationContext)
                .enqueueUniquePeriodicWork(
                        TAG,
                        ExistingPeriodicWorkPolicy.KEEP,
                        new PeriodicWorkRequest.Builder(CleanupWorker.class, 1, TimeUnit.DAYS)
                                .build());
    }

    public static void initialize(Context applicationContext, int logLevel) {
        instance = new DatabaseLogger(applicationContext, logLevel);
    }

    public static DatabaseLogger getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseLogger is not initialized");
        }
        return instance;
    }

    public static void logDetection(String message) {
        DatabaseLogger instance = getInstance();
        addLogEntry(instance.detectionLogDao, DetectionLog::new, message, instance.executor);
    }

    public static void logMessage(Message message) {
        DatabaseLogger instance = getInstance();
        addLogEntry(
                instance.messagesLogDao,
                MessagesLog::new,
                String.format(MESSAGE_LOG_FORMAT, message.getTopic(), message.getContent()),
                instance.executor);
    }

    public static void v(String tag, String message) {
        log(Log.VERBOSE, tag, message, null);
    }

    public static void v(String tag, String message, Throwable throwable) {
        log(Log.VERBOSE, tag, message, throwable);
    }

    public static void d(String tag, String message) {
        log(Log.DEBUG, tag, message, null);
    }

    public static void d(String tag, String message, Throwable throwable) {
        log(Log.DEBUG, tag, message, throwable);
    }

    public static void i(String tag, String message) {
        log(Log.INFO, tag, message, null);
    }

    public static void i(String tag, String message, Throwable throwable) {
        log(Log.INFO, tag, message, throwable);
    }

    public static void w(String tag, String message) {
        log(Log.WARN, tag, message, null);
    }

    public static void w(String tag, String message, Throwable throwable) {
        log(Log.WARN, tag, message, throwable);
    }

    public static void e(String tag, String message) {
        log(Log.ERROR, tag, message, null);
    }

    public static void e(String tag, String message, Throwable throwable) {
        log(Log.ERROR, tag, message, throwable);
    }

    private static void log(int level, String tag, String message, @Nullable Throwable throwable) {
        DatabaseLogger instance = getInstance();
        if (level >= instance.logLevel) {
            String line;
            if (throwable != null) {
                line = message + '\n' + Log.getStackTraceString(throwable);
            } else {
                line = message;
            }
            Log.println(level, tag, line);
            addLogEntry(
                    instance.developerLogDao,
                    DeveloperLog::new,
                    String.format(DEVELOPER_LOG_FORMAT, getLogLevelName(level), tag, line),
                    instance.executor);
        }
    }

    private static <T extends DbLog> void addLogEntry(
            DbLogDao<T> dao, EntityFactory<T> factory, String message, Executor executor) {
        Date now = new Date();
        T entity = factory.create(0, now.getTime(), getFormattedLog(message, now));
        ListenableFuture<Long> result = dao.insert(entity);
        result.addListener(
                () -> {
                    try {
                        result.get();
                    } catch (ExecutionException e) {
                        Log.e(TAG, "Unable to write to log table", e.getCause());
                    } catch (Exception e) {
                        Log.e(TAG, "Unable to write to log table", e);
                    }
                },
                executor);
    }

    @NonNull
    public static String getFormattedLog(String message, Date now) {
        return String.format(Locale.ROOT, MESSAGE_FORMAT, now, message);
    }

    private static String getLogLevelName(int logLevel) {
        switch (logLevel) {
            case Log.VERBOSE:
                return "VERBOSE";
            case Log.DEBUG:
                return "DEBUG";
            case Log.INFO:
                return "INFO";
            case Log.WARN:
                return "WARN";
            case Log.ERROR:
                return "ERROR";
            case Log.ASSERT:
                return "ASSERT";
            default:
                return "NONE";
        }
    }

    public DetectionLogDao getDetectionLogDao() {
        return detectionLogDao;
    }

    public MessagesLogDao getMessagesLogDao() {
        return messagesLogDao;
    }

    public DeveloperLogDao getDeveloperLogDao() {
        return developerLogDao;
    }

    private interface EntityFactory<T> {
        T create(long id, long timestamp, String message);
    }
}
