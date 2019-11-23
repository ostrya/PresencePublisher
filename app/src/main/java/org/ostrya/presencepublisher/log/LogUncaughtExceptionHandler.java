package org.ostrya.presencepublisher.log;

import android.util.Log;
import androidx.annotation.Nullable;
import com.hypertrack.hyperlog.HyperLog;

public class LogUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "LogUncaughtExceptionHandler";
    @Nullable
    private final Thread.UncaughtExceptionHandler originalHandler;

    public LogUncaughtExceptionHandler(@Nullable Thread.UncaughtExceptionHandler originalHandler) {
        this.originalHandler = originalHandler;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        HyperLog.e(TAG, t + " crashed due to " + Log.getStackTraceString(e));
        if (originalHandler != null) {
            originalHandler.uncaughtException(t, e);
        }
    }
}
