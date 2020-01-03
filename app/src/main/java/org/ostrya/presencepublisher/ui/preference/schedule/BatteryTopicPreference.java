package org.ostrya.presencepublisher.ui.preference.schedule;

import android.content.Context;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

import static org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider.PreferenceType.STRING;

public class BatteryTopicPreference extends TextPreferenceBase {
    public static final String BATTERY_TOPIC = "batteryTopic";

    public BatteryTopicPreference(Context context) {
        super(context, BATTERY_TOPIC, new RegexValidator("[^ ]+"), R.string.battery_topic_title);
    }

    @Override
    protected void configureSummary() {
        setSummaryProvider(new ExplanationSummaryProvider<>(R.string.battery_topic_summary, STRING));
    }
}
