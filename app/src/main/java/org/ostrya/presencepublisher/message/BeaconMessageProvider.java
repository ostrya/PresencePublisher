package org.ostrya.presencepublisher.message;

import static org.ostrya.presencepublisher.beacon.RegionMonitorNotifier.FOUND_BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_CONTENT_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport.BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.schedule.PresenceTopicPreference.PRESENCE_TOPIC;

import android.content.Context;

import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.PresencePublisher;
import org.ostrya.presencepublisher.ui.preference.condition.BeaconPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BeaconMessageProvider extends AbstractMessageProvider {
    private static final String TAG = "BeaconMessageProvider";

    public BeaconMessageProvider(Context context) {
        super(context, PRESENCE_TOPIC);
    }

    @Override
    public List<String> getMessageContents() {
        if (!((PresencePublisher) getApplicationContext()).supportsBeacons()
                || getSharedPreferences()
                        .getStringSet(BEACON_LIST, Collections.emptySet())
                        .isEmpty()) {
            return Collections.emptyList();
        }
        HyperLog.i(TAG, "Checking found beacons");
        List<String> contents = new ArrayList<>();
        for (String beaconId :
                getSharedPreferences().getStringSet(FOUND_BEACON_LIST, Collections.emptySet())) {
            HyperLog.i(TAG, "Scheduling message for beacon " + beaconId);
            String content =
                    getSharedPreferences()
                            .getString(
                                    BEACON_CONTENT_PREFIX + beaconId,
                                    BeaconPreference.DEFAULT_CONTENT_ONLINE);
            contents.add(content);
        }
        return contents;
    }
}
