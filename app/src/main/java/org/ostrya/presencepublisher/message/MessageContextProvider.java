package org.ostrya.presencepublisher.message;

import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.network.NetworkService;

public class MessageContextProvider {
    private final AlarmclockTimestampProvider alarmclockTimestampProvider;
    private final BatteryStatusProvider batteryStatusProvider;
    private final ConditionContentProvider conditionContentProvider;
    private final LocationProvider locationProvider;
    private final NetworkService networkService;
    private final SharedPreferences preferences;

    public MessageContextProvider(Context applicationContext) {
        this.alarmclockTimestampProvider = new AlarmclockTimestampProvider(applicationContext);
        this.batteryStatusProvider = new BatteryStatusProvider(applicationContext);
        this.conditionContentProvider = new ConditionContentProvider(applicationContext);
        this.locationProvider = new LocationProvider(applicationContext);
        this.networkService = new NetworkService(applicationContext);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    }

    public MessageContext getContext() {
        long currentTimestamp = System.currentTimeMillis();
        String currentSsid = networkService.getCurrentSsid();
        return new MessageContext(
                alarmclockTimestampProvider.getNextAlarmclockTimestamp(),
                batteryStatusProvider.getCurrentBatteryStatus(),
                conditionContentProvider.getConditionContents(currentSsid),
                locationProvider.getLastKnownLocation(),
                preferences.getLong(LAST_SUCCESS, 0L),
                currentTimestamp,
                preferences.getLong(NEXT_SCHEDULE, 0L),
                currentSsid);
    }
}
