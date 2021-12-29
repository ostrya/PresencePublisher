package org.ostrya.presencepublisher.ui.preference.connection;

import android.content.Context;

import androidx.preference.ListPreference;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;

public class QoSPreference extends ListPreference {
    public static final String QOS_VALUE = "qos";
    public static final int DEFAULT_VALUE = 1;

    public QoSPreference(Context context) {
        super(context);
        setKey(QOS_VALUE);
        setIconSpaceReserved(false);
        setTitle(R.string.qos_value_title);
        setSummaryProvider(new ExplanationSummaryProvider<>(R.string.qos_value_summary));
        setEntries(R.array.qos_descriptions);
        setEntryValues(R.array.qos_values);
        setDefaultValue(Integer.toString(DEFAULT_VALUE));
    }
}
