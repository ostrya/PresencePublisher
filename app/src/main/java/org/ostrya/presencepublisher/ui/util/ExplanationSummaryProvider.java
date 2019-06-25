package org.ostrya.presencepublisher.ui.util;

import androidx.preference.Preference;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.R;

import java.util.Collections;

public class ExplanationSummaryProvider<T extends Preference> implements Preference.SummaryProvider<T> {
    private static final String TAG = "ExplanationSummaryProvider";
    private final int summaryId;
    private final PreferenceType type;

    public ExplanationSummaryProvider(int summaryId, PreferenceType type) {
        this.summaryId = summaryId;
        this.type = type;
    }

    @Override
    public CharSequence provideSummary(T preference) {
        return String.format(preference.getContext().getString(summaryId), getValue(preference));
    }

    private String getValue(T preference) {
        String undefined = preference.getContext().getString(R.string.value_undefined);
        if (preference.hasKey()) {
            switch (type) {
                case STRING:
                    return preference.getSharedPreferences().getString(preference.getKey(), undefined);
                case LIST:
                    return preference.getSharedPreferences().getStringSet(preference.getKey(), Collections.emptySet()).toString();
                default:
                    HyperLog.e(TAG, "Unexpected type received: " + type);
                    throw new IllegalArgumentException("Unexpected type");
            }
        } else {
            return undefined;
        }
    }

    public enum PreferenceType {
        STRING,
        LIST
    }
}
