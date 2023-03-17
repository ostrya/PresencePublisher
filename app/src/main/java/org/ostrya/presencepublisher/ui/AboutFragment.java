package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.about.BundledLicensesPreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference;
import org.ostrya.presencepublisher.ui.preference.about.NightModePreference;
import org.ostrya.presencepublisher.ui.preference.about.PrivacyPreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.about.SignaturePreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.about.SourceRepositoryPreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.about.VersionInfoPreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.common.MyPreferenceCategory;
import org.ostrya.presencepublisher.ui.util.AbstractConfigurationFragment;

public class AboutFragment extends AbstractConfigurationFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        Context context = getPreferenceManager().getContext();

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        PreferenceCategory legalCategory =
                new MyPreferenceCategory(context, R.string.category_legal);
        PreferenceCategory uiCategory = new MyPreferenceCategory(context, R.string.category_ui);
        PreferenceCategory appCategory = new MyPreferenceCategory(context, R.string.category_build);

        screen.addPreference(legalCategory);
        screen.addPreference(uiCategory);
        screen.addPreference(appCategory);

        legalCategory.addPreference(new SourceRepositoryPreferenceDummy(context, this));
        legalCategory.addPreference(new BundledLicensesPreferenceDummy(context, this));
        legalCategory.addPreference(new PrivacyPreferenceDummy(context, this));
        legalCategory.addPreference(new LocationConsentPreference(context));

        uiCategory.addPreference(new NightModePreference(context));

        appCategory.addPreference(new VersionInfoPreferenceDummy(context));
        appCategory.addPreference(new SignaturePreferenceDummy(context));

        setPreferenceScreen(screen);
    }

    @Override
    protected void onPreferencesChanged(SharedPreferences preferences, String name) {
        if (NightModePreference.NIGHT_MODE.equals(name)) {
            NightModePreference.updateCurrentNightMode(requireContext());
        }
    }
}
