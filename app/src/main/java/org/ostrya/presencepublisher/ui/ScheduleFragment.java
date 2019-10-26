package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.EditTextPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.message.wifi.SsidUtil;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;
import org.ostrya.presencepublisher.ui.util.RegexValidator;
import org.ostrya.presencepublisher.ui.util.TimestampSummaryProvider;

import java.util.List;

import static org.ostrya.presencepublisher.ui.ConnectionFragment.PING;
import static org.ostrya.presencepublisher.ui.util.EditTextPreferencesHelper.getEditTextPreference;
import static org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider.PreferenceType.LIST;

public class ScheduleFragment extends PreferenceFragmentCompat {
    /**
     * @deprecated old parameter from before v1.5, use SSID_LIST instead
     */
    @Deprecated
    public static final String SSID = "ssid";
    public static final String SSID_LIST = "ssids";
    public static final String AUTOSTART = "autostart";
    public static final String LAST_PING = "lastPing";
    public static final String NEXT_PING = "nextPing";
    public static final String OFFLINE_PING = "offlinePing";
    public static final String MOBILE_NETWORK_PING = "mobileNetworkPing";
    public static final String BATTERY_MESSAGE = "batteryMessage";
    public static final String BATTERY_TOPIC = "batteryTopic";
    private Preference lastPing;
    private Preference nextPing;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener = this::onPreferencesChanged;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

        List<String> knownSsids = SsidUtil.getKnownSsids(context);
        String[] entryValues = knownSsids.toArray(new String[0]);
        MultiSelectListPreference ssidList = new MultiSelectListPreference(context);
        ssidList.setKey(SSID_LIST);
        ssidList.setTitle(R.string.ssid_title);
        ssidList.setSummaryProvider(new ExplanationSummaryProvider(R.string.ssid_summary, LIST));
        ssidList.setEntryValues(entryValues);
        ssidList.setEntries(entryValues);
        ssidList.setIconSpaceReserved(false);

        SeekBarPreference ping = new SeekBarPreference(context);
        ping.setKey(PING);
        ping.setMin(1);
        ping.setMax(30);
        ping.setDefaultValue(15);
        ping.setSeekBarIncrement(1);
        ping.setShowSeekBarValue(true);
        ping.setTitle(getString(R.string.ping_title));
        ping.setSummary(getString(R.string.ping_summary));
        ping.setIconSpaceReserved(false);

        SwitchPreferenceCompat sendOfflinePing = new SwitchPreferenceCompat(context);
        sendOfflinePing.setKey(OFFLINE_PING);
        sendOfflinePing.setTitle(getString(R.string.offlineping_title));
        sendOfflinePing.setSummary(R.string.offlineping_summary);
        sendOfflinePing.setIconSpaceReserved(false);

        SwitchPreferenceCompat sendMobileNetworkPing = new SwitchPreferenceCompat(context);
        sendMobileNetworkPing.setKey(MOBILE_NETWORK_PING);
        sendMobileNetworkPing.setTitle(getString(R.string.offlineping_via_mobile_title));
        sendMobileNetworkPing.setSummary(R.string.offlineping_via_mobile_summary);
        sendMobileNetworkPing.setIconSpaceReserved(false);

        SwitchPreferenceCompat sendBatteryMessage = new SwitchPreferenceCompat(context);
        sendBatteryMessage.setKey(BATTERY_MESSAGE);
        sendBatteryMessage.setTitle(getString(R.string.battery_message_title));
        sendBatteryMessage.setSummary(R.string.battery_message_summary);
        sendBatteryMessage.setIconSpaceReserved(false);

        EditTextPreference batteryTopic = getEditTextPreference(context, BATTERY_TOPIC, R.string.battery_topic_title, R.string.battery_topic_summary, new RegexValidator("[^ ]+"));

        SwitchPreferenceCompat autostart = new SwitchPreferenceCompat(context);
        autostart.setKey(AUTOSTART);
        autostart.setTitle(getString(R.string.autostart_title));
        autostart.setSummary(R.string.autostart_summary);
        autostart.setIconSpaceReserved(false);

        lastPing = new Preference(context);
        lastPing.setKey(LAST_PING);
        lastPing.setTitle(R.string.last_ping_title);
        lastPing.setSummaryProvider(new TimestampSummaryProvider());
        lastPing.setIconSpaceReserved(false);

        nextPing = new Preference(context);
        nextPing.setKey(NEXT_PING);
        nextPing.setTitle(R.string.next_ping_title);
        nextPing.setSummaryProvider(new TimestampSummaryProvider());
        nextPing.setIconSpaceReserved(false);

        screen.addPreference(ssidList);
        screen.addPreference(ping);
        screen.addPreference(sendOfflinePing);
        screen.addPreference(sendMobileNetworkPing);
        screen.addPreference(sendBatteryMessage);
        screen.addPreference(batteryTopic);
        screen.addPreference(autostart);
        screen.addPreference(lastPing);
        screen.addPreference(nextPing);

        setPreferenceScreen(screen);

        sendMobileNetworkPing.setDependency(OFFLINE_PING);
        batteryTopic.setDependency(BATTERY_MESSAGE);
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
            case LAST_PING:
                poorMansRefresh(lastPing);
                break;
            case NEXT_PING:
                poorMansRefresh(nextPing);
                break;
            default:
                break;
        }
    }

    private void poorMansRefresh(Preference preference) {
        if (preference != null) {
            boolean copyingEnabled = preference.isCopyingEnabled();
            preference.setCopyingEnabled(!copyingEnabled);
            preference.setCopyingEnabled(copyingEnabled);
        }
    }
}
