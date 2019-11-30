package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.AddNetworkChoicePreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.ContentHelpDummy;
import org.ostrya.presencepublisher.ui.preference.MyPreferenceCategory;
import org.ostrya.presencepublisher.ui.preference.OfflineContentPreference;
import org.ostrya.presencepublisher.ui.preference.SendOfflineMessagePreference;
import org.ostrya.presencepublisher.ui.preference.SendViaMobileNetworkPreference;
import org.ostrya.presencepublisher.ui.preference.WifiNetworkPreference;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.ostrya.presencepublisher.ui.preference.AddNetworkChoicePreferenceDummy.SSID_LIST;
import static org.ostrya.presencepublisher.ui.preference.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.WifiNetworkPreference.WIFI_CONTENT_PREFIX;

public class ConditionFragment extends PreferenceFragmentCompat {

    private Context context;
    private AddNetworkChoicePreferenceDummy addNetworkChoice;
    private PreferenceCategory wifiCategory;
    private Set<String> currentNetworks;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener = this::onSsidListChanged;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        context = getPreferenceManager().getContext();
        SharedPreferences preference = getPreferenceManager().getSharedPreferences();
        currentNetworks = Collections.unmodifiableSet(preference.getStringSet(SSID_LIST, Collections.emptySet()));

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference contentHelp = new ContentHelpDummy(context);
        addNetworkChoice = new AddNetworkChoicePreferenceDummy(context, preference, this);
        Preference sendOfflineMessage = new SendOfflineMessagePreference(context);
        Preference offlineContent = new OfflineContentPreference(context);
        Preference sendViaMobileNetwork = new SendViaMobileNetworkPreference(context);

        wifiCategory = new MyPreferenceCategory(context, R.string.category_wifi);
        PreferenceCategory offlineCategory = new MyPreferenceCategory(context, R.string.category_offline);

        screen.addPreference(contentHelp);
        screen.addPreference(wifiCategory);
        screen.addPreference(offlineCategory);

        wifiCategory.setOrderingAsAdded(false);

        for (String ssid : currentNetworks) {
            wifiCategory.addPreference(new WifiNetworkPreference(context, ssid, preference, this));
        }
        wifiCategory.addPreference(addNetworkChoice);

        offlineCategory.addPreference(sendOfflineMessage);
        offlineCategory.addPreference(offlineContent);
        offlineCategory.addPreference(sendViaMobileNetwork);

        setPreferenceScreen(screen);

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

        sendViaMobileNetwork.setDependency(SEND_OFFLINE_MESSAGE);
        offlineContent.setDependency(SEND_OFFLINE_MESSAGE);
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

    private void onSsidListChanged(SharedPreferences sharedPreferences, String key) {
        if (SSID_LIST.equals(key)) {
            Set<String> changedNetworks = Collections.unmodifiableSet(sharedPreferences.getStringSet(key, Collections.emptySet()));
            Set<String> removed = new HashSet<>(currentNetworks);
            removed.removeAll(changedNetworks);
            Set<String> added = new HashSet<>(changedNetworks);
            added.removeAll(currentNetworks);
            currentNetworks = changedNetworks;
            for (String add : added) {
                wifiCategory.addPreference(new WifiNetworkPreference(context, add, sharedPreferences, this));
            }
            for (String remove : removed) {
                wifiCategory.removePreferenceRecursively(WIFI_CONTENT_PREFIX + remove);
            }
            addNetworkChoice.updateVisibleEntries(currentNetworks);
        }
    }
}
