package org.ostrya.presencepublisher.preference;

import static org.ostrya.presencepublisher.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import org.ostrya.presencepublisher.IntentActionContract;
import org.ostrya.presencepublisher.PresencePublisher;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.preference.common.AbstractConfigurationFragment;
import org.ostrya.presencepublisher.preference.common.MyPreferenceCategory;
import org.ostrya.presencepublisher.preference.common.StringDummy;
import org.ostrya.presencepublisher.preference.condition.BeaconCategorySupport;
import org.ostrya.presencepublisher.preference.condition.OfflineContentPreference;
import org.ostrya.presencepublisher.preference.condition.SendOfflineMessagePreference;
import org.ostrya.presencepublisher.preference.condition.SendViaMobileNetworkPreference;
import org.ostrya.presencepublisher.preference.condition.WifiCategorySupport;

import java.util.Map;

public class ConditionFragment extends AbstractConfigurationFragment {
    private static final String TAG = "ConditionFragment";

    private BeaconCategorySupport beaconSupport;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        Context context = getPreferenceManager().getContext();
        boolean beaconsSupported =
                ((PresencePublisher) context.getApplicationContext()).supportsBeacons();
        SharedPreferences preference = getPreferenceManager().getSharedPreferences();

        WifiCategorySupport wifiSupport = new WifiCategorySupport(this);
        ActivityResultLauncher<String> serviceStartLauncher;
        ActivityResultLauncher<String[]> permissionRequestLauncher;
        // to make linter happy
        if (beaconsSupported) {
            serviceStartLauncher =
                    registerForActivityResult(new IntentActionContract(), this::onServiceStart);
            permissionRequestLauncher =
                    registerForActivityResult(
                            new ActivityResultContracts.RequestMultiplePermissions(),
                            this::onPermissionsGranted);
        } else {
            serviceStartLauncher = null;
            permissionRequestLauncher = null;
        }
        beaconSupport =
                new BeaconCategorySupport(this, serviceStartLauncher, permissionRequestLauncher);

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference contentHelp = new StringDummy(context, R.string.condition_help_summary);
        Preference sendViaMobileNetwork = new SendViaMobileNetworkPreference(context);
        PreferenceCategory wifiCategory = wifiSupport.getCategory();
        PreferenceCategory beaconCategory = beaconSupport.getCategory();
        PreferenceCategory offlineCategory =
                new MyPreferenceCategory(context, R.string.category_offline);

        screen.addPreference(contentHelp);
        screen.addPreference(sendViaMobileNetwork);
        screen.addPreference(wifiCategory);
        screen.addPreference(beaconCategory);
        screen.addPreference(offlineCategory);

        wifiSupport.initializeCategory();
        beaconSupport.initializeCategory();
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
        if (LOCATION_CONSENT.equals(key)) {
            getPreferenceScreen().setEnabled(sharedPreferences.getBoolean(LOCATION_CONSENT, false));
        }
    }

    private void onServiceStart(boolean result) {
        DatabaseLogger.d(TAG, "Received result " + result);
        if (result && beaconSupport != null) {
            DatabaseLogger.i(TAG, "Start scanning after enabling bluetooth");
            beaconSupport.clickAdd();
        }
    }

    private void onPermissionsGranted(Map<String, Boolean> result) {
        DatabaseLogger.d(TAG, "Received result " + result);
        if (result.values().stream().allMatch(b -> b) && beaconSupport != null) {
            DatabaseLogger.i(TAG, "Start scanning after enabling bluetooth");
            beaconSupport.clickAdd();
        }
    }
}
