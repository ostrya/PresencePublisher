package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.ContentHelpDummy;
import org.ostrya.presencepublisher.ui.preference.MyPreferenceCategory;
import org.ostrya.presencepublisher.ui.preference.OfflineContentPreference;
import org.ostrya.presencepublisher.ui.preference.WifiContentPreference;

import java.util.Collections;

import static org.ostrya.presencepublisher.ui.preference.SsidListPreference.SSID_LIST;

public class ContentFragment extends PreferenceFragmentCompat {

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference contentHelp = new ContentHelpDummy(context);

        PreferenceCategory wifiCategory = new MyPreferenceCategory(context, R.string.content_category_wifi);

        PreferenceCategory otherCategory = new MyPreferenceCategory(context, R.string.content_category_other);

        screen.addPreference(contentHelp);
        screen.addPreference(wifiCategory);
        screen.addPreference(otherCategory);

        fillWifiCategory(context, wifiCategory);

        Preference offlineContent = new OfflineContentPreference(context);

        otherCategory.addPreference(offlineContent);

        setPreferenceScreen(screen);

        listener = (sharedPreferences, key) -> {
            if (SSID_LIST.equals(key)) {
                wifiCategory.removeAll();
                fillWifiCategory(context, wifiCategory);
            }
        };
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
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

    private void fillWifiCategory(Context context, PreferenceCategory wifi) {
        for (String ssid : getPreferenceManager().getSharedPreferences().getStringSet(SSID_LIST, Collections.emptySet())) {
            wifi.addPreference(new WifiContentPreference(context, ssid));
        }
    }
}
