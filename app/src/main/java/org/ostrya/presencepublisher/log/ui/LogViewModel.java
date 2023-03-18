package org.ostrya.presencepublisher.log.ui;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.log.LogAccessor;
import org.ostrya.presencepublisher.log.LogItem;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class LogViewModel extends ViewModel {
    private final Map<LogType, LogAccessor<?>> accessorMap = new EnumMap<>(LogType.class);
    private LogAccessor<?> logAccessor;

    public LogViewModel() {
        DatabaseLogger dbLogger = DatabaseLogger.getInstance();
        accessorMap.put(LogType.DEVELOPER, new LogAccessor<>(dbLogger.getDeveloperLogDao()));
        accessorMap.put(LogType.MESSAGES, new LogAccessor<>(dbLogger.getMessagesLogDao()));
        accessorMap.put(LogType.DETECTION, new LogAccessor<>(dbLogger.getDetectionLogDao()));
        logAccessor = accessorMap.get(LogType.MESSAGES);
    }

    public LiveData<List<LogItem>> getLogItems() {
        return logAccessor.getLogs();
    }

    public void setLogType(LogType logType) {
        logAccessor = accessorMap.get(logType);
    }

    public void clearLogs() {
        logAccessor.clear();
    }

    public Future<File> exportLogs(Context context) {
        return logAccessor.exportLogs(context);
    }
}
