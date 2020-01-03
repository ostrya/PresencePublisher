package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.Application;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.ContentHelpDummy;
import org.ostrya.presencepublisher.ui.preference.common.MyPreferenceCategory;
import org.ostrya.presencepublisher.ui.preference.condition.AddBeaconChoicePreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.condition.AddNetworkChoicePreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.condition.BeaconPreference;
import org.ostrya.presencepublisher.ui.preference.condition.OfflineContentPreference;
import org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference;
import org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference;
import org.ostrya.presencepublisher.ui.preference.condition.WifiNetworkPreference;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.ostrya.presencepublisher.Application.BLUETOOTH_REQUEST_CODE;
import static org.ostrya.presencepublisher.ui.preference.condition.AddBeaconChoicePreferenceDummy.BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.AddNetworkChoicePreferenceDummy.SSID_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconPreference.BEACON_CONTENT_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.WifiNetworkPreference.WIFI_CONTENT_PREFIX;

public class ConditionFragment extends PreferenceFragmentCompat {
    private static final String TAG = "ConditionFragment";

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = this::onPreferencesChanged;
    private Context context;
    private boolean beaconsSupported = false;
    private AddNetworkChoicePreferenceDummy addNetworkChoice;
    @Nullable
    private AddBeaconChoicePreferenceDummy addBeaconChoice;
    private PreferenceCategory wifiCategory;
    private Set<String> currentNetworks;
    private PreferenceCategory beaconCategory;
    private Set<String> currentBeacons;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        context = getPreferenceManager().getContext();
        beaconsSupported = ((Application) context.getApplicationContext()).supportsBeacons();
        SharedPreferences preference = getPreferenceManager().getSharedPreferences();
        currentNetworks = Collections.unmodifiableSet(preference.getStringSet(SSID_LIST, Collections.emptySet()));
        currentBeacons = Collections.unmodifiableSet(preference.getStringSet(BEACON_LIST, Collections.emptySet()));

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference contentHelp = new ContentHelpDummy(context, R.string.condition_help_summary);
        Preference sendViaMobileNetwork = new SendViaMobileNetworkPreference(context);
        wifiCategory = new MyPreferenceCategory(context, R.string.category_wifi);
        beaconCategory = new MyPreferenceCategory(context, R.string.category_beacon_regions);
        PreferenceCategory offlineCategory = new MyPreferenceCategory(context, R.string.category_offline);

        screen.addPreference(contentHelp);
        screen.addPreference(sendViaMobileNetwork);
        screen.addPreference(wifiCategory);
        screen.addPreference(beaconCategory);
        screen.addPreference(offlineCategory);

        wifiCategory.setOrderingAsAdded(false);
        addNetworkChoice = new AddNetworkChoicePreferenceDummy(context, preference, this);
        for (String ssid : currentNetworks) {
            wifiCategory.addPreference(new WifiNetworkPreference(context, ssid, preference, this));
        }
        wifiCategory.addPreference(addNetworkChoice);

        beaconCategory.setOrderingAsAdded(false);
        // to make linter happy
        if (beaconsSupported && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            addBeaconChoice = new AddBeaconChoicePreferenceDummy(context, this);
            for (String beaconId : currentBeacons) {
                beaconCategory.addPreference(new BeaconPreference(context, beaconId, this));
            }
            beaconCategory.addPreference(addBeaconChoice);
        } else {
            beaconCategory.addPreference(new ContentHelpDummy(context, R.string.no_bluetooth_explanation));
        }

        Preference offlineContent = new OfflineContentPreference(context);
        offlineCategory.addPreference(new SendOfflineMessagePreference(context));
        offlineCategory.addPreference(offlineContent);

        setPreferenceScreen(screen);

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

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

    private void onPreferencesChanged(SharedPreferences sharedPreferences, String key) {
        if (SSID_LIST.equals(key)) {
            Set<String> changedNetworks = Collections.unmodifiableSet(sharedPreferences.getStringSet(SSID_LIST, Collections.emptySet()));
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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && BEACON_LIST.equals(key)) {
            Set<String> changedBeacons = Collections.unmodifiableSet(sharedPreferences.getStringSet(BEACON_LIST, Collections.emptySet()));
            Set<String> removed = new HashSet<>(currentBeacons);
            removed.removeAll(changedBeacons);
            Set<String> added = new HashSet<>(changedBeacons);
            added.removeAll(currentBeacons);
            currentBeacons = changedBeacons;
            for (String add : added) {
                beaconCategory.addPreference(new BeaconPreference(context, add, this));
            }
            for (String remove : removed) {
                beaconCategory.removePreferenceRecursively(BEACON_CONTENT_PREFIX + remove);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        HyperLog.d(TAG, "Received result " + resultCode + " for " + requestCode);
        if (requestCode == BLUETOOTH_REQUEST_CODE && addBeaconChoice != null) {
            HyperLog.i(TAG, "Start scanning after enabling bluetooth");
            addBeaconChoice.performClick();
        }
    }

    public interface RequestResultListener {
        void onRequestResult(int requestCode, int resultCode);
    }
}
