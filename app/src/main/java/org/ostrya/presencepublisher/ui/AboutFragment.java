package org.ostrya.presencepublisher.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.about.AppIdPreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.about.BuildTypePreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.about.BundledLicensesPreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.about.PrivacyPreferenceDummy;
import org.ostrya.presencepublisher.ui.preference.about.SourceRepositoryPreference;
import org.ostrya.presencepublisher.ui.preference.about.VersionCodePreference;
import org.ostrya.presencepublisher.ui.preference.about.VersionNamePreference;
import org.ostrya.presencepublisher.ui.preference.common.MyPreferenceCategory;

public class AboutFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        PreferenceCategory legalCategory = new MyPreferenceCategory(context, R.string.category_legal);
        PreferenceCategory appCategory = new MyPreferenceCategory(context, R.string.category_build);

        screen.addPreference(legalCategory);
        screen.addPreference(appCategory);

        legalCategory.addPreference(new SourceRepositoryPreference(context, this));
        legalCategory.addPreference(new BundledLicensesPreferenceDummy(context, this));
        legalCategory.addPreference(new PrivacyPreferenceDummy(context, this));

        appCategory.addPreference(new AppIdPreferenceDummy(context));
        appCategory.addPreference(new VersionNamePreference(context));
        appCategory.addPreference(new VersionCodePreference(context));
        appCategory.addPreference(new BuildTypePreferenceDummy(context));

        setPreferenceScreen(screen);
    }
}
