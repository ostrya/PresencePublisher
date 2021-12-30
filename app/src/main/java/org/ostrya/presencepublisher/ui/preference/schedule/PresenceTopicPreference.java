package org.ostrya.presencepublisher.ui.preference.schedule;

import static org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider.PreferenceType.STRING;

import android.content.Context;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

public class PresenceTopicPreference extends TextPreferenceBase {
    public static final String PRESENCE_TOPIC = "topic";

    public PresenceTopicPreference(Context context) {
        super(context, PRESENCE_TOPIC, new RegexValidator("[^ ]+"), R.string.presence_topic_title);
    }

    @Override
    protected void configureSummary() {
        setSummaryProvider(
                new ExplanationSummaryProvider<>(R.string.presence_topic_summary, STRING));
    }
}
