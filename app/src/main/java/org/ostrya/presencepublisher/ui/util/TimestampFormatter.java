package org.ostrya.presencepublisher.ui.util;

import android.content.Context;

import org.ostrya.presencepublisher.R;

import java.text.DateFormat;
import java.util.Date;

public class TimestampFormatter {
    public static final long UNDEFINED = 0L;

    public static String format(Context context, long timestamp) {
        String undefined = context.getString(R.string.value_undefined);
        if (timestamp == UNDEFINED) {
            return undefined;
        } else {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(new Date(timestamp));
        }
    }
}
