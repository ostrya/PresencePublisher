package org.ostrya.presencepublisher.preference.common;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import org.ostrya.presencepublisher.schedule.TimestampFormatter;

public class TimestampPreferenceBase extends Preference
        implements Preference.SummaryProvider<TimestampPreferenceBase> {
    public TimestampPreferenceBase(Context context, String key, int titleId) {
        super(context);
        setKey(key);
        setTitle(titleId);
        setSummaryProvider(this);
        setIconSpaceReserved(false);
    }

    public void refresh() {
        boolean copyingEnabled = isCopyingEnabled();
        setCopyingEnabled(!copyingEnabled);
        setCopyingEnabled(copyingEnabled);
    }

    @Nullable
    @Override
    public CharSequence provideSummary(@NonNull TimestampPreferenceBase preference) {
        long timestamp = TimestampFormatter.UNDEFINED;
        if (preference.hasKey()) {
            timestamp =
                    preference
                            .getSharedPreferences()
                            .getLong(preference.getKey(), TimestampFormatter.UNDEFINED);
        }
        return TimestampFormatter.format(preference.getContext(), timestamp);
    }
}
