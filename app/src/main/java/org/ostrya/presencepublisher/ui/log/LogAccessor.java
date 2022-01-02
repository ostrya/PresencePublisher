package org.ostrya.presencepublisher.ui.log;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

public interface LogAccessor<T extends LogItem> {
    LiveData<List<T>> getLogs();

    Future<File> exportLogs(Context context);

    void clear();
}
