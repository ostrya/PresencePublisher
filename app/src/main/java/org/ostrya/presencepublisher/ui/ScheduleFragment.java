package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import org.ostrya.presencepublisher.ui.preference.schedule.AutostartPreference;
import org.ostrya.presencepublisher.ui.preference.schedule.BatteryTopicPreference;
import org.ostrya.presencepublisher.ui.preference.schedule.ChargingMessageSchedulePreference;
import org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference;
import org.ostrya.presencepublisher.ui.preference.schedule.MessageSchedulePreference;
import org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference;
import org.ostrya.presencepublisher.ui.preference.schedule.PresenceTopicPreference;
import org.ostrya.presencepublisher.ui.preference.schedule.SendBatteryMessagePreference;

import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;
import static org.ostrya.presencepublisher.ui.preference.schedule.SendBatteryMessagePreference.SEND_BATTERY_MESSAGE;

public class ScheduleFragment extends PreferenceFragmentCompat {
    private final SharedPreferences.OnSharedPreferenceChangeListener listener = this::onPreferencesChanged;
    private LastSuccessTimestampPreference lastSuccess;
    private NextScheduleTimestampPreference nextSchedule;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

        Preference messageSchedule = new MessageSchedulePreference(context);
        Preference chargingMessageSchedule = new ChargingMessageSchedulePreference(context);
        Preference presenceTopic = new PresenceTopicPreference(context);

        Preference sendBatteryMessage = new SendBatteryMessagePreference(context);
        Preference batteryTopic = new BatteryTopicPreference(context);

        Preference autostart = new AutostartPreference(context);

        lastSuccess = new LastSuccessTimestampPreference(context);
        nextSchedule = new NextScheduleTimestampPreference(context);

        screen.addPreference(messageSchedule);
        screen.addPreference(chargingMessageSchedule);
        screen.addPreference(presenceTopic);
        screen.addPreference(sendBatteryMessage);
        screen.addPreference(batteryTopic);
        screen.addPreference(autostart);
        screen.addPreference(lastSuccess);
        screen.addPreference(nextSchedule);

        setPreferenceScreen(screen);

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
        lastSuccess.refresh();
        nextSchedule.refresh();
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
