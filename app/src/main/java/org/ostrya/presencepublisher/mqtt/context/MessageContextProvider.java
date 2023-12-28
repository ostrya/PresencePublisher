package org.ostrya.presencepublisher.mqtt.context;

import static org.ostrya.presencepublisher.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.mqtt.context.condition.ConditionContentProvider;
import org.ostrya.presencepublisher.mqtt.context.condition.network.NetworkService;
import org.ostrya.presencepublisher.mqtt.context.device.AlarmclockTimestampProvider;
import org.ostrya.presencepublisher.mqtt.context.device.BatteryStatusProvider;
import org.ostrya.presencepublisher.mqtt.context.device.DeviceNameProvider;
import org.ostrya.presencepublisher.mqtt.context.device.LocationProvider;

public class MessageContextProvider {
    private final AlarmclockTimestampProvider alarmclockTimestampProvider;
    private final BatteryStatusProvider batteryStatusProvider;
    private final ConditionContentProvider conditionContentProvider;
    private final DeviceNameProvider deviceNameProvider;
    private final LocationProvider locationProvider;
    private final NetworkService networkService;
    private final SharedPreferences preferences;

    public MessageContextProvider(Context applicationContext) {
        this.alarmclockTimestampProvider = new AlarmclockTimestampProvider(applicationContext);
        this.batteryStatusProvider = new BatteryStatusProvider(applicationContext);
        this.conditionContentProvider = new ConditionContentProvider(applicationContext);
        this.deviceNameProvider = new DeviceNameProvider(applicationContext);
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
                deviceNameProvider.getDeviceName(),
                currentSsid);
    }
}
