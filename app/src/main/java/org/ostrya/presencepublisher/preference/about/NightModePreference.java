package org.ostrya.presencepublisher.preference.about;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.ListPreferenceBase;

public class NightModePreference extends ListPreferenceBase {
    public static final String NIGHT_MODE = "nightMode";

    public NightModePreference(Context context) {
        super(
                context,
                NIGHT_MODE,
                R.string.night_mode_title,
                Integer.toString(getDefaultNightMode()),
                R.string.content_summary);
        setEntries(R.array.night_mode_descriptions);
        setEntryValues(R.array.night_mode_values);
    }

    public static void updateCurrentNightMode(Context context) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String nightModeValue = preferences.getString(NightModePreference.NIGHT_MODE, null);
        int nightMode;
        try {
            nightMode = Integer.parseInt(nightModeValue);
        } catch (NumberFormatException e) {
            nightMode = getDefaultNightMode();
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    private static int getDefaultNightMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        } else {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
    }
}
