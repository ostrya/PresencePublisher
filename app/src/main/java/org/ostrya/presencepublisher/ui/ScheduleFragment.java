package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import org.ostrya.presencepublisher.ui.preference.AutostartPreference;
import org.ostrya.presencepublisher.ui.preference.BatteryTopicPreference;
import org.ostrya.presencepublisher.ui.preference.LastSuccessTimestampPreference;
import org.ostrya.presencepublisher.ui.preference.MessageSchedulePreference;
import org.ostrya.presencepublisher.ui.preference.NextScheduleTimestampPreference;
import org.ostrya.presencepublisher.ui.preference.SendBatteryMessagePreference;
import org.ostrya.presencepublisher.ui.preference.SendOfflineMessagePreference;
import org.ostrya.presencepublisher.ui.preference.SendViaMobileNetworkPreference;
import org.ostrya.presencepublisher.ui.preference.SsidListPreference;

import static org.ostrya.presencepublisher.ui.preference.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.NextScheduleTimestampPreference.NEXT_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.SendBatteryMessagePreference.SEND_BATTERY_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;

public class ScheduleFragment extends PreferenceFragmentCompat {
    /**
     * @deprecated old parameter from before v1.5, use SSID_LIST instead
     */
    @Deprecated
    public static final String SSID = "ssid";

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = this::onPreferencesChanged;
    private LastSuccessTimestampPreference lastSuccess;
    private NextScheduleTimestampPreference nextSchedule;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

        Preference ssidList = new SsidListPreference(context);
        Preference messageSchedule = new MessageSchedulePreference(context);

        Preference sendOfflineMessage = new SendOfflineMessagePreference(context);
        Preference sendViaMobileNetwork = new SendViaMobileNetworkPreference(context);

        Preference sendBatteryMessage = new SendBatteryMessagePreference(context);
        Preference batteryTopic = new BatteryTopicPreference(context);

        Preference autostart = new AutostartPreference(context);

        lastSuccess = new LastSuccessTimestampPreference(context);
        nextSchedule = new NextScheduleTimestampPreference(context);

        screen.addPreference(ssidList);
        screen.addPreference(messageSchedule);
        screen.addPreference(sendOfflineMessage);
        screen.addPreference(sendViaMobileNetwork);
        screen.addPreference(sendBatteryMessage);
        screen.addPreference(batteryTopic);
        screen.addPreference(autostart);
        screen.addPreference(lastSuccess);
        screen.addPreference(nextSchedule);

        setPreferenceScreen(screen);

        sendViaMobileNetwork.setDependency(SEND_OFFLINE_MESSAGE);
        batteryTopic.setDependency(SEND_BATTERY_MESSAGE);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    private void onPreferencesChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case LAST_SUCCESS:
                lastSuccess.refresh();
                break;
            case NEXT_SCHEDULE:
                nextSchedule.refresh();
                break;
            default:
                break;
        }
    }
}
