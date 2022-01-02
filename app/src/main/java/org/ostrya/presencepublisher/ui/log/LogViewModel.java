package org.ostrya.presencepublisher.ui.log;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class LogViewModel extends ViewModel {
    private final Map<LogType, LogAccessor<? extends LogItem>> accessorMap =
            new EnumMap<>(LogType.class);
    private LogAccessor<? extends LogItem> logAccessor;

    public LogViewModel() {
        DatabaseLogger dbLogger = DatabaseLogger.getInstance();
        accessorMap.put(LogType.DEVELOPER, new DbLogAccessor<>(dbLogger.getDeveloperLogDao()));
        accessorMap.put(LogType.MESSAGES, new DbLogAccessor<>(dbLogger.getMessagesLogDao()));
        accessorMap.put(LogType.DETECTION, new DbLogAccessor<>(dbLogger.getDetectionLogDao()));
        logAccessor = accessorMap.get(LogType.MESSAGES);
    }

    public LiveData<? extends List<? extends LogItem>> getLogItems() {
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
