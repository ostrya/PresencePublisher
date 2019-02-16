package org.ostrya.presencepublisher.ui.util;

import androidx.preference.Preference;
import org.ostrya.presencepublisher.R;

import java.text.DateFormat;
import java.util.Date;

public class TimestampSummaryProvider<T extends Preference> implements Preference.SummaryProvider<T> {
    @Override
    public CharSequence provideSummary(final T preference) {
        String undefined = preference.getContext().getString(R.string.value_undefined);
        if (preference.hasKey()) {
            long timestamp = preference.getSharedPreferences().getLong(preference.getKey(), 0L);
            if (timestamp == 0L) {
                return undefined;
            }
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(timestamp));
        } else {
            return undefined;
        }
    }
}
