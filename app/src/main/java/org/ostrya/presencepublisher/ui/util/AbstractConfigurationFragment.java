package org.ostrya.presencepublisher.ui.util;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.preference.PreferenceFragmentCompat;

public abstract class AbstractConfigurationFragment extends PreferenceFragmentCompat {
    private final SharedPreferences.OnSharedPreferenceChangeListener listener = this::onPreferencesChanged;

    @Override
    @CallSuper
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    @CallSuper
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    @CallSuper
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    protected abstract void onPreferencesChanged(SharedPreferences preferences, String name);
}
