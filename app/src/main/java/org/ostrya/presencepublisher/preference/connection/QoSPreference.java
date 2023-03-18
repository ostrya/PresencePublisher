package org.ostrya.presencepublisher.preference.connection;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.ListPreferenceBase;

public class QoSPreference extends ListPreferenceBase {
    public static final String QOS_VALUE = "qos";
    public static final int DEFAULT_VALUE = 1;

    public QoSPreference(Context context) {
        super(
                context,
                QOS_VALUE,
                R.string.qos_value_title,
                Integer.toString(DEFAULT_VALUE),
                R.string.qos_value_summary);
        setEntries(R.array.qos_descriptions);
        setEntryValues(R.array.qos_values);
    }
}
