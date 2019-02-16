package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;
import org.ostrya.presencepublisher.ui.util.RegexValidator;
import org.ostrya.presencepublisher.ui.util.TimestampSummaryProvider;
import org.ostrya.presencepublisher.util.SsidUtil;

import java.util.List;

import static org.ostrya.presencepublisher.ui.ConnectionFragment.PING;
import static org.ostrya.presencepublisher.ui.util.EditTextPreferencesHelper.getEditTextPreference;

public class ScheduleFragment extends PreferenceFragmentCompat {
    public static final String SSID = "ssid";
    public static final String AUTOSTART = "autostart";
    public static final String LAST_PING = "lastPing";
    public static final String NEXT_PING = "nextPing";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference ssid;
        List<String> knownSsids = SsidUtil.getKnownSsids(context);
        if (knownSsids == null) {
            ssid = getEditTextPreference(context, SSID, R.string.ssid_title, R.string.ssid_summary, new RegexValidator(".+"));
        } else {
            String[] entryValues = knownSsids.toArray(new String[knownSsids.size()]);
            ListPreference ssidList = new ListPreference(context);
            ssidList.setKey(SSID);
            ssidList.setTitle(R.string.ssid_title);
            ssidList.setSummaryProvider(new ExplanationSummaryProvider(R.string.ssid_summary));
            ssidList.setEntryValues(entryValues);
            ssidList.setEntries(entryValues);
            ssidList.setIconSpaceReserved(false);
            ssid = ssidList;
        }

        SeekBarPreference ping = new SeekBarPreference(context);
        ping.setKey(PING);
        ping.setMin(1);
        ping.setMax(30);
        ping.setSeekBarIncrement(1);
        ping.setTitle(getString(R.string.ping_title));
        ping.setSummary(getString(R.string.ping_summary));
        ping.setIconSpaceReserved(false);

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

        screen.addPreference(ssid);
        screen.addPreference(ping);
        screen.addPreference(autostart);
        screen.addPreference(lastPing);
        screen.addPreference(nextPing);

        setPreferenceScreen(screen);
    }
}
