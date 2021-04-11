package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.PresencePublisher;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.contract.IntentActionContract;
import org.ostrya.presencepublisher.ui.preference.common.MyPreferenceCategory;
import org.ostrya.presencepublisher.ui.preference.common.StringDummy;
import org.ostrya.presencepublisher.ui.preference.condition.AddBeaconChoicePreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.condition.AddNetworkChoicePreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.condition.BeaconPreference;
import org.ostrya.presencepublisher.ui.preference.condition.OfflineContentPreference;
import org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference;
import org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference;
import org.ostrya.presencepublisher.ui.preference.condition.WifiNetworkPreference;
import org.ostrya.presencepublisher.ui.util.AbstractConfigurationFragment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.ui.preference.condition.AddBeaconChoicePreferenceDummy.BEACON_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.AddNetworkChoicePreferenceDummy.SSID_LIST;
import static org.ostrya.presencepublisher.ui.preference.condition.BeaconPreference.BEACON_CONTENT_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;
import static org.ostrya.presencepublisher.ui.preference.condition.WifiNetworkPreference.WIFI_CONTENT_PREFIX;

public class ConditionFragment extends AbstractConfigurationFragment {
    private static final String TAG = "ConditionFragment";

    @Nullable
    private AddBeaconChoicePreferenceDummy addBeaconChoice;
    private PreferenceCategory wifiCategory;
    private Set<String> currentNetworks;
    private PreferenceCategory beaconCategory;
    private Set<String> currentBeacons;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        Context context = getPreferenceManager().getContext();
        boolean beaconsSupported = ((PresencePublisher) context.getApplicationContext()).supportsBeacons();
        SharedPreferences preference = getPreferenceManager().getSharedPreferences();
        currentNetworks = Collections.unmodifiableSet(preference.getStringSet(SSID_LIST, Collections.emptySet()));
        currentBeacons = Collections.unmodifiableSet(preference.getStringSet(BEACON_LIST, Collections.emptySet()));

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference contentHelp = new StringDummy(context, R.string.condition_help_summary);
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
        for (String ssid : currentNetworks) {
            wifiCategory.addPreference(new WifiNetworkPreference(context, ssid, preference, this));
        }
        wifiCategory.addPreference(new AddNetworkChoicePreferenceDummy(context, preference, this));

        beaconCategory.setOrderingAsAdded(false);
        // to make linter happy
        if (beaconsSupported && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ActivityResultLauncher<String> intentLauncher
                    = registerForActivityResult(new IntentActionContract(), this::onActivityResult);
            addBeaconChoice = new AddBeaconChoicePreferenceDummy(context, this, intentLauncher);
            for (String beaconId : currentBeacons) {
                beaconCategory.addPreference(new BeaconPreference(context, beaconId, this));
            }
            beaconCategory.addPreference(addBeaconChoice);
        } else {
            beaconCategory.addPreference(new StringDummy(context, R.string.no_bluetooth_explanation));
        }

        Preference offlineContent = new OfflineContentPreference(context);
        offlineCategory.addPreference(new SendOfflineMessagePreference(context));
        offlineCategory.addPreference(offlineContent);

        setPreferenceScreen(screen);

        offlineContent.setDependency(SEND_OFFLINE_MESSAGE);
        screen.setEnabled(preference.getBoolean(LOCATION_CONSENT, false));
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        getPreferenceScreen().setEnabled(preferences.getBoolean(LOCATION_CONSENT, false));
    }

    @Override
    protected void onPreferencesChanged(SharedPreferences sharedPreferences, String key) {
        Context context = getPreferenceManager().getContext();
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
        } else if (LOCATION_CONSENT.equals(key)) {
            getPreferenceScreen().setEnabled(sharedPreferences.getBoolean(LOCATION_CONSENT, false));
        }
    }

    private void onActivityResult(boolean result) {
        HyperLog.d(TAG, "Received result " + result);
        if (result && addBeaconChoice != null) {
            HyperLog.i(TAG, "Start scanning after enabling bluetooth");
            addBeaconChoice.getOnPreferenceClickListener().onPreferenceClick(addBeaconChoice);
        }
    }
}
