package org.ostrya.presencepublisher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.*;

import java.text.DateFormat;
import java.util.Date;

public class SettingsFragment extends PreferenceFragmentCompat {

    static final String URL = "url";
    static final String TOPIC = "topic";
    static final String PING = "ping";
    static final String SSID = "ssid";
    static final String AUTOSTART = "autostart";
    static final String LAST_PING = "lastPing";
    static final String NEXT_PING = "nextPing";

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        Context context = getPreferenceManager().getContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        EditTextPreference url = getEditTextPreference(URL, R.string.url_title, R.string.url_summary);

        EditTextPreference topic = getEditTextPreference(TOPIC, R.string.topic_title, R.string.topic_summary);

        EditTextPreference ssid = getEditTextPreference(SSID, R.string.ssid_title, R.string.ssid_summary);

        SeekBarPreference ping = new SeekBarPreference(context);
        ping.setKey(PING);
        ping.setMin(1);
        ping.setMax(30);
        ping.setSeekBarIncrement(1);
        ping.setTitle(getString(org.ostrya.presencepublisher.R.string.ping_title));
        ping.setSummary(getString(org.ostrya.presencepublisher.R.string.ping_summary));

        SwitchPreferenceCompat autostart = new SwitchPreferenceCompat(context);
        autostart.setKey(AUTOSTART);
        autostart.setTitle(getString(R.string.autostart_title));

        Preference lastPing = new Preference(context);
        lastPing.setKey(LAST_PING);
        lastPing.setTitle(context.getString(R.string.last_ping_title));
        lastPing.setSummary(getFormattedLastPing(sharedPreferences, LAST_PING));
        sharedPreferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
            if (LAST_PING.equals(key)) {
                lastPing.setSummary(getFormattedLastPing(sharedPreferences, LAST_PING));
            }
        });

        Preference nextPing = new Preference(context);
        nextPing.setKey(NEXT_PING);
        nextPing.setTitle(context.getString(R.string.next_ping_title));
        nextPing.setSummary(getFormattedLastPing(sharedPreferences, NEXT_PING));
        sharedPreferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
            if (LAST_PING.equals(key)) {
                nextPing.setSummary(getFormattedLastPing(sharedPreferences, NEXT_PING));
            }
        });

        screen.addPreference(url);
        screen.addPreference(topic);
        screen.addPreference(ssid);
        screen.addPreference(ping);
        screen.addPreference(autostart);
        screen.addPreference(lastPing);
        screen.addPreference(nextPing);

        setPreferenceScreen(screen);
    }

    private String getFormattedLastPing(final SharedPreferences sharedPreferences, final String key) {
        return DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(sharedPreferences.getLong(key, 0L)));
    }

    private EditTextPreference getEditTextPreference(final String key, final int titleId, final int summaryId) {
        Context context = getPreferenceManager().getContext();
        EditTextPreference preference = new EditTextPreference(context);
        preference.setKey(key);
        preference.setTitle(getString(titleId));
        preference.setSummary(String.format(getString(summaryId), sharedPreferences.getString(key, "undefined")));
        preference.setOnPreferenceChangeListener((prefs, newValue) -> {
            prefs.setSummary(String.format(getString(summaryId), newValue));
            return true;
        });
        return preference;
    }

    private SwitchPreferenceCompat getSwitchPreferenceCompat(final String key, final int titleId, final int summaryId) {
        Context context = getPreferenceManager().getContext();
        SwitchPreferenceCompat preference = new SwitchPreferenceCompat(context);
        preference.setKey(key);
        preference.setTitle(getString(titleId));
        preference.setSummary(String.format(getString(summaryId), sharedPreferences.getBoolean(key, false)));
        preference.setOnPreferenceChangeListener((prefs, newValue) -> {
            prefs.setSummary(String.format(getString(summaryId), newValue));
            return true;
        });
        return preference;
    }
}
