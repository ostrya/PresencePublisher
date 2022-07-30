package org.ostrya.presencepublisher.ui;

import static org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import org.ostrya.presencepublisher.ui.preference.schedule.AutostartPreference;
import org.ostrya.presencepublisher.ui.preference.schedule.ChargingMessageSchedulePreference;
import org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference;
import org.ostrya.presencepublisher.ui.preference.schedule.MessageSchedulePreference;
import org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference;
import org.ostrya.presencepublisher.ui.preference.schedule.RunNowPreferenceDummy;
import org.ostrya.presencepublisher.ui.util.AbstractConfigurationFragment;

public class ScheduleFragment extends AbstractConfigurationFragment {
    private LastSuccessTimestampPreference lastSuccess;
    private NextScheduleTimestampPreference nextSchedule;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference messageSchedule = new MessageSchedulePreference(context);
        Preference chargingMessageSchedule = new ChargingMessageSchedulePreference(context);

        Preference autostart = new AutostartPreference(context);

        lastSuccess = new LastSuccessTimestampPreference(context);
        nextSchedule = new NextScheduleTimestampPreference(context);

        Preference runNowDummy = new RunNowPreferenceDummy(context, this);

        screen.addPreference(messageSchedule);
        screen.addPreference(chargingMessageSchedule);
        screen.addPreference(autostart);
        screen.addPreference(lastSuccess);
        screen.addPreference(nextSchedule);
        screen.addPreference(runNowDummy);

        setPreferenceScreen(screen);
        SharedPreferences preference = getPreferenceManager().getSharedPreferences();
        screen.setEnabled(preference.getBoolean(LOCATION_CONSENT, false));
    }

    @Override
    public void onResume() {
        super.onResume();
        lastSuccess.refresh();
        nextSchedule.refresh();
        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        getPreferenceScreen().setEnabled(preferences.getBoolean(LOCATION_CONSENT, false));
    }

    @Override
    protected void onPreferencesChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case LAST_SUCCESS:
                lastSuccess.refresh();
                break;
            case NEXT_SCHEDULE:
                nextSchedule.refresh();
                break;
            case LOCATION_CONSENT:
                getPreferenceScreen()
                        .setEnabled(sharedPreferences.getBoolean(LOCATION_CONSENT, false));
                break;
            default:
                break;
        }
    }
}
