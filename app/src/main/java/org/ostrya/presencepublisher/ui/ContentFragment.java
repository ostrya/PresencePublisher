package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

import java.util.Collections;

import static org.ostrya.presencepublisher.ui.ScheduleFragment.SSID_LIST;
import static org.ostrya.presencepublisher.ui.util.EditTextPreferencesHelper.getEditTextPreference;

public class ContentFragment extends PreferenceFragmentCompat {
    public static final String DEFAULT_CONTENT_ONLINE = "online";
    public static final String DEFAULT_CONTENT_OFFLINE = "offline";
    public static final String WIFI_PREFIX = "wifi.";
    public static final String CONTENT_OFFLINE = "offlineContent";

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference help = new Preference(context);
        help.setTitle(null);
        help.setSummary(R.string.content_help);
        help.setIconSpaceReserved(false);

        PreferenceCategory wifi = new PreferenceCategory(context);
        wifi.setIconSpaceReserved(false);
        wifi.setTitle(R.string.content_category_wifi);

        PreferenceCategory other = new PreferenceCategory(context);
        other.setIconSpaceReserved(false);
        other.setTitle(R.string.content_category_other);

        screen.addPreference(help);
        screen.addPreference(wifi);
        screen.addPreference(other);

        RegexValidator notEmptyValidator = new RegexValidator(".+");
        fillWifiCategory(context, wifi, notEmptyValidator);

        EditTextPreference offline = getEditTextPreference(context, CONTENT_OFFLINE, getString(R.string.offline_content_title),
                R.string.content_summary, DEFAULT_CONTENT_OFFLINE, notEmptyValidator);

        other.addPreference(offline);

        setPreferenceScreen(screen);

        listener = (sharedPreferences, key) -> {
            if (SSID_LIST.equals(key)) {
                wifi.removeAll();
                fillWifiCategory(context, wifi, notEmptyValidator);
            }
        };
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    private void fillWifiCategory(Context context, PreferenceCategory wifi, RegexValidator notEmptyValidator) {
        for (String ssid : getPreferenceManager().getSharedPreferences().getStringSet(SSID_LIST, Collections.emptySet())) {
            wifi.addPreference(getEditTextPreference(context, WIFI_PREFIX + ssid, ssid, R.string.content_summary,
                    DEFAULT_CONTENT_ONLINE, notEmptyValidator));
        }
    }
}
