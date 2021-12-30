package org.ostrya.presencepublisher.log;

import android.content.Context;

import com.hypertrack.hyperlog.LogFormat;

public class CustomLogFormat extends LogFormat {
    public CustomLogFormat(Context context) {
        super(context);
    }

    @Override
    public String getFormattedLogMessage(
            String logLevelName,
            String tag,
            String message,
            String timeStamp,
            String senderName,
            String osVersion,
            String deviceUUID) {
        return timeStamp + " [" + logLevelName + "/" + tag + "]: " + message;
    }
}
