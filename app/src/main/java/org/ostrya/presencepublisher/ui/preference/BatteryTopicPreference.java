package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

public class BatteryTopicPreference extends AbstractTextPreference {
    public static final String BATTERY_TOPIC = "batteryTopic";

    public BatteryTopicPreference(Context context) {
        super(context, BATTERY_TOPIC, new RegexValidator("[^ ]+"), R.string.battery_topic_title, R.string.battery_topic_summary);
    }
}
