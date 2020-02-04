package org.ostrya.presencepublisher.log;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.hypertrack.hyperlog.HyperLog;

import java.util.Objects;

public class LogUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "LogUncaughtExceptionHandler";

    @NonNull
    private final Context context;
    @Nullable
    private final Thread.UncaughtExceptionHandler originalHandler;

    public LogUncaughtExceptionHandler(@NonNull final Context context, @Nullable Thread.UncaughtExceptionHandler originalHandler) {
        this.context = Objects.requireNonNull(context);
        this.originalHandler = originalHandler;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        final String message = t + " crashed due to " + Log.getStackTraceString(e);
        HyperLog.e(TAG, message);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.setType("plain/text");

        context.startActivity(sendIntent);

        if (originalHandler != null) {
            originalHandler.uncaughtException(t, e);
        }
    }
}
