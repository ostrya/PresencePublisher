package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.PresencePublisher;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.contract.IntentActionContract;
import org.ostrya.presencepublisher.ui.preference.common.MyPreferenceCategory;
import org.ostrya.presencepublisher.ui.preference.common.StringDummy;
import org.ostrya.presencepublisher.ui.preference.condition.BeaconCategorySupport;
import org.ostrya.presencepublisher.ui.preference.condition.OfflineContentPreference;
import org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference;
import org.ostrya.presencepublisher.ui.preference.condition.SendViaMobileNetworkPreference;
import org.ostrya.presencepublisher.ui.preference.condition.WifiCategorySupport;
import org.ostrya.presencepublisher.ui.util.AbstractConfigurationFragment;

import static org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference.LOCATION_CONSENT;
import static org.ostrya.presencepublisher.ui.preference.condition.SendOfflineMessagePreference.SEND_OFFLINE_MESSAGE;

public class ConditionFragment extends AbstractConfigurationFragment {
    private static final String TAG = "ConditionFragment";

    private BeaconCategorySupport beaconSupport;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        Context context = getPreferenceManager().getContext();
        boolean beaconsSupported = ((PresencePublisher) context.getApplicationContext()).supportsBeacons();
        SharedPreferences preference = getPreferenceManager().getSharedPreferences();

        WifiCategorySupport wifiSupport = new WifiCategorySupport(this);
        ActivityResultLauncher<String> intentLauncher;
        // to make linter happy
        if (beaconsSupported && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intentLauncher = registerForActivityResult(new IntentActionContract(), this::onActivityResult);
        } else {
            intentLauncher = null;
        }
        beaconSupport = new BeaconCategorySupport(this, intentLauncher);

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference contentHelp = new StringDummy(context, R.string.condition_help_summary);
        Preference sendViaMobileNetwork = new SendViaMobileNetworkPreference(context);
        PreferenceCategory wifiCategory = wifiSupport.getCategory();
        PreferenceCategory beaconCategory = beaconSupport.getCategory();
        PreferenceCategory offlineCategory = new MyPreferenceCategory(context, R.string.category_offline);

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

    private void onActivityResult(boolean result) {
        HyperLog.d(TAG, "Received result " + result);
        if (result && beaconSupport != null) {
            HyperLog.i(TAG, "Start scanning after enabling bluetooth");
            beaconSupport.clickAdd();
        }
    }
}
