package org.ostrya.presencepublisher.log;

import static org.ostrya.presencepublisher.log.FormatArgs.args;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;

import org.ostrya.presencepublisher.log.db.DbLog;
import org.ostrya.presencepublisher.log.db.DetectionLog;
import org.ostrya.presencepublisher.log.db.DetectionLogDao;
import org.ostrya.presencepublisher.log.db.DeveloperLog;
import org.ostrya.presencepublisher.log.db.DeveloperLogDao;
import org.ostrya.presencepublisher.log.db.LogDao;
import org.ostrya.presencepublisher.log.db.LogDatabase;
import org.ostrya.presencepublisher.log.db.MessagesLog;
import org.ostrya.presencepublisher.log.db.MessagesLogDao;
import org.ostrya.presencepublisher.mqtt.message.Message;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DatabaseLogger implements AutoCloseable {
    private static final String TAG = "DatabaseLogger";
    private static final String DEVELOPER_MESSAGE_FORMAT = "%tF %<tT.%<tL (%d:%s) [%s/%s]: %s";
    private static final String MESSAGE_FORMAT = "%tF %<tT.%<tL: %s";
    private static final String MESSAGE_LOG_FORMAT = "Sent to topic '%s':%n%s";
    private static final FormatArgs EMPTY = args();
    private static DatabaseLogger instance = null;

    private final int logLevel;
    private final ExecutorService executor = Executors.newCachedThreadPool();
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
                        ExistingPeriodicWorkPolicy.UPDATE,
                        new PeriodicWorkRequest.Builder(
                                        CleanupWorker.class, 1, TimeUnit.DAYS, 1, TimeUnit.HOURS)
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
        addLogEntry(
                instance.detectionLogDao,
                DetectionLog::new,
                DatabaseLogger::messageWithTimestamp,
                message,
                instance.executor);
    }

    public static void logMessage(Message message) {
        DatabaseLogger instance = getInstance();
        addLogEntry(
                instance.messagesLogDao,
                MessagesLog::new,
                DatabaseLogger::messageWithTimestamp,
                String.format(MESSAGE_LOG_FORMAT, message.getTopic(), message.getContent()),
                instance.executor);
    }

    public static void logMessageError(String error) {
        DatabaseLogger instance = getInstance();
        addLogEntry(
                instance.messagesLogDao,
                MessagesLog::new,
                DatabaseLogger::messageWithTimestamp,
                error,
                instance.executor);
    }

    public static void v(String tag, String message) {
        log(Log.VERBOSE, tag, message, EMPTY, null);
    }

    public static void v(String tag, String message, Throwable throwable) {
        log(Log.VERBOSE, tag, message, EMPTY, throwable);
    }

    public static void v(String tag, String message, FormatArgs args) {
        log(Log.VERBOSE, tag, message, args, null);
    }

    public static void v(String tag, String message, FormatArgs args, Throwable throwable) {
        log(Log.VERBOSE, tag, message, args, throwable);
    }

    public static void d(String tag, String message) {
        log(Log.DEBUG, tag, message, EMPTY, null);
    }

    public static void d(String tag, String message, Throwable throwable) {
        log(Log.DEBUG, tag, message, EMPTY, throwable);
    }

    public static void d(String tag, String message, FormatArgs args) {
        log(Log.DEBUG, tag, message, args, null);
    }

    public static void d(String tag, String message, FormatArgs args, Throwable throwable) {
        log(Log.DEBUG, tag, message, args, throwable);
    }

    public static void i(String tag, String message) {
        log(Log.INFO, tag, message, EMPTY, null);
    }

    public static void i(String tag, String message, Throwable throwable) {
        log(Log.INFO, tag, message, EMPTY, throwable);
    }

    public static void i(String tag, String message, FormatArgs args) {
        log(Log.INFO, tag, message, args, null);
    }

    public static void i(String tag, String message, FormatArgs args, Throwable throwable) {
        log(Log.INFO, tag, message, args, throwable);
    }

    public static void w(String tag, String message) {
        log(Log.WARN, tag, message, EMPTY, null);
    }

    public static void w(String tag, String message, Throwable throwable) {
        log(Log.WARN, tag, message, EMPTY, throwable);
    }

    public static void w(String tag, String message, FormatArgs args) {
        log(Log.WARN, tag, message, args, null);
    }

    public static void w(String tag, String message, FormatArgs args, Throwable throwable) {
        log(Log.WARN, tag, message, args, throwable);
    }

    public static void e(String tag, String message) {
        log(Log.ERROR, tag, message, EMPTY, null);
    }

    public static void e(String tag, String message, Throwable throwable) {
        log(Log.ERROR, tag, message, EMPTY, throwable);
    }

    public static void e(String tag, String message, FormatArgs args) {
        log(Log.ERROR, tag, message, args, null);
    }

    public static void e(String tag, String message, FormatArgs args, Throwable throwable) {
        log(Log.ERROR, tag, message, args, throwable);
    }

    private static void log(
            int level, String tag, String message, FormatArgs args, @Nullable Throwable throwable) {
        DatabaseLogger instance = getInstance();
        if (level >= instance.logLevel) {
            String line;
            if (throwable != null) {
                line =
                        args.applyTo(message)
                                + ": "
                                + throwable.getMessage()
                                + '\n'
                                + Log.getStackTraceString(throwable);
            } else {
                line = args.applyTo(message);
            }
            Log.println(level, tag, line);
            addLogEntry(
                    instance.developerLogDao,
                    DeveloperLog::new,
                    new DeveloperLogMessageFactory(level, tag),
                    line,
                    instance.executor);
        }
    }

    private static <T extends DbLog> void addLogEntry(
            LogDao<T> dao,
            EntityFactory<T> entityFactory,
            MessageFactory messageFactory,
            String message,
            Executor executor) {
        Date now = new Date();
        T entity = entityFactory.create(0, now.getTime(), messageFactory.format(message, now));
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

    public DetectionLogDao getDetectionLogDao() {
        return detectionLogDao;
    }

    public MessagesLogDao getMessagesLogDao() {
        return messagesLogDao;
    }

    public DeveloperLogDao getDeveloperLogDao() {
        return developerLogDao;
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    private interface EntityFactory<T> {
        T create(long id, long timestamp, String message);
    }

    private interface MessageFactory {
        String format(String message, Date now);
    }

    private static String messageWithTimestamp(String message, Date now) {
        return String.format(Locale.ROOT, MESSAGE_FORMAT, now, message);
    }

    private static class DeveloperLogMessageFactory implements MessageFactory {
        private final int level;
        private final String tag;

        private DeveloperLogMessageFactory(int level, String tag) {
            this.level = level;
            this.tag = tag;
        }

        @Override
        public String format(String message, Date now) {
            Thread currentThread = Thread.currentThread();
            return String.format(
                    Locale.ROOT,
                    DEVELOPER_MESSAGE_FORMAT,
                    now,
                    currentThread.getId(),
                    currentThread.getName(),
                    getLogLevelName(level),
                    tag,
                    message);
        }
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
}
