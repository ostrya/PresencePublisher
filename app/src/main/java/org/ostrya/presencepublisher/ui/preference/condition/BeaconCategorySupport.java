package org.ostrya.presencepublisher.ui.preference.condition;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.ui.preference.common.AbstractDynamicPreferenceCategorySupport;
import org.ostrya.presencepublisher.ui.preference.common.StringDummy;
import org.ostrya.presencepublisher.ui.util.AbstractConfigurationFragment;

public class BeaconCategorySupport extends AbstractDynamicPreferenceCategorySupport {
    public static final String BEACON_LIST = "beacons";
    public static final String BEACON_CONTENT_PREFIX = "beacon.";

    private static final String TAG = "BeaconCategorySupport";

    public BeaconCategorySupport(
            AbstractConfigurationFragment fragment,
            @Nullable ActivityResultLauncher<String> intentLauncher) {
        super(
                fragment,
                R.string.category_beacon_regions,
                BEACON_LIST,
                BEACON_CONTENT_PREFIX,
                (c, p, f) -> createAdderEntry(c, f, intentLauncher),
                BeaconCategorySupport::createEntry);
    }

    private static Preference createAdderEntry(
            Context context,
            Fragment fragment,
            @Nullable ActivityResultLauncher<String> intentLauncher) {
        if (intentLauncher != null) {
            return new AddBeaconChoicePreferenceDummy(context, fragment, intentLauncher);
        } else {
            return new StringDummy(context, R.string.no_bluetooth_explanation);
        }
    }

    private static Preference createEntry(
            Context context,
            String key,
            String title,
            SharedPreferences preferences,
            Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return new BeaconPreference(context, key, title, fragment);
        } else {
            DatabaseLogger.w(
                    TAG,
                    "Should never happen: tried to create beacon entries on incompatible devices!");
            return new StringDummy(context, R.string.no_bluetooth_explanation);
        }
    }

    public void clickAdd() {
        @Nullable
        Preference adderPreference =
                getPreferenceManager()
                        .findPreference(AddBeaconChoicePreferenceDummy.class.getCanonicalName());
        if (adderPreference != null) {
            adderPreference.getOnPreferenceClickListener().onPreferenceClick(adderPreference);
        }
    }
}
