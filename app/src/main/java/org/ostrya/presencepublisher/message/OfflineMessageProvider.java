package org.ostrya.presencepublisher.message;

import android.content.Context;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.ui.preference.condition.OfflineContentPreference;

import java.util.Collections;
import java.util.List;

import static org.ostrya.presencepublisher.ui.preference.schedule.PresenceTopicPreference.PRESENCE_TOPIC;

public class OfflineMessageProvider extends AbstractMessageProvider {
    private static final String TAG = "OfflineMessageProvider";

    public OfflineMessageProvider(Context context) {
        super(context, PRESENCE_TOPIC);
    }

    @Override
    protected List<String> getMessageContents() {
        HyperLog.i(TAG, "Scheduling offline message");
        String offlineContent = getSharedPreferences().getString(OfflineContentPreference.OFFLINE_CONTENT, OfflineContentPreference.DEFAULT_CONTENT_OFFLINE);
        return Collections.singletonList(offlineContent);
    }
}
