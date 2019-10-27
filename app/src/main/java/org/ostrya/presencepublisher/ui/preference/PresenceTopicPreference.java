package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

public class PresenceTopicPreference extends AbstractTextPreference {
    public static final String PRESENCE_TOPIC = "topic";

    public PresenceTopicPreference(Context context) {
        super(context, PRESENCE_TOPIC, new RegexValidator("[^ ]+"), R.string.presence_topic_title, R.string.presence_topic_summary);
    }
}
