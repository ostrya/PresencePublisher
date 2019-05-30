package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;
import org.ostrya.presencepublisher.ui.util.TimestampSummaryProvider;
import org.ostrya.presencepublisher.util.SsidUtil;

import java.util.List;

import static org.ostrya.presencepublisher.ui.ConnectionFragment.PING;
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

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        List<String> knownSsids = SsidUtil.getKnownSsids(context);
        String[] entryValues = knownSsids.toArray(new String[knownSsids.size()]);
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

        SwitchPreferenceCompat autostart = new SwitchPreferenceCompat(context);
        autostart.setKey(AUTOSTART);
        autostart.setTitle(getString(R.string.autostart_title));
        autostart.setSummary(R.string.autostart_summary);
        autostart.setIconSpaceReserved(false);

        Preference lastPing = new Preference(context);
        lastPing.setKey(LAST_PING);
        lastPing.setTitle(R.string.last_ping_title);
        lastPing.setSummaryProvider(new TimestampSummaryProvider());
        lastPing.setIconSpaceReserved(false);

        Preference nextPing = new Preference(context);
        nextPing.setKey(NEXT_PING);
        nextPing.setTitle(R.string.next_ping_title);
        nextPing.setSummaryProvider(new TimestampSummaryProvider());
        nextPing.setIconSpaceReserved(false);

        screen.addPreference(ssidList);
        screen.addPreference(ping);
        screen.addPreference(sendOfflinePing);
        screen.addPreference(autostart);
        screen.addPreference(lastPing);
        screen.addPreference(nextPing);

        setPreferenceScreen(screen);
    }
}
