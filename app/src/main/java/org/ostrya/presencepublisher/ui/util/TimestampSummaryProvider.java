package org.ostrya.presencepublisher.ui.util;

import android.content.Context;
import androidx.preference.Preference;
import org.ostrya.presencepublisher.R;

import java.text.DateFormat;
import java.util.Date;

public class TimestampSummaryProvider<T extends Preference> implements Preference.SummaryProvider<T> {
    public static final long WAITING_FOR_RECONNECT = -1L;
    public static final long UNDEFINED = 0L;

    public static String getFormattedTimestamp(Context context, long timestamp) {
        String undefined = context.getString(R.string.value_undefined);
        if (timestamp == UNDEFINED) {
            return undefined;
        } else if (timestamp == WAITING_FOR_RECONNECT) {
            return context.getString(R.string.value_network_reconnect);
        } else {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(timestamp));
        }
    }

    @Override
    public CharSequence provideSummary(final T preference) {
        long timestamp = UNDEFINED;
        if (preference.hasKey()) {
            timestamp = preference.getSharedPreferences().getLong(preference.getKey(), UNDEFINED);
        }
        return getFormattedTimestamp(preference.getContext(), timestamp);
    }
}
