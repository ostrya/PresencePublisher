package org.ostrya.presencepublisher.preference.common;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConfigurationFragment extends PreferenceFragmentCompat {
    private final SharedPreferences.OnSharedPreferenceChangeListener ownListener =
            this::onPreferencesChanged;
    private final List<SharedPreferences.OnSharedPreferenceChangeListener> listeners =
            new ArrayList<>();

    @Override
    @CallSuper
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(ownListener);
    }

    @Override
    @CallSuper
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(ownListener);
        for (SharedPreferences.OnSharedPreferenceChangeListener listener : listeners) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    @Override
    @CallSuper
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(ownListener);
        for (SharedPreferences.OnSharedPreferenceChangeListener listener : listeners) {
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    public void addListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        listeners.add(listener);
        getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(listener);
    }

    protected abstract void onPreferencesChanged(SharedPreferences preferences, String name);
}
