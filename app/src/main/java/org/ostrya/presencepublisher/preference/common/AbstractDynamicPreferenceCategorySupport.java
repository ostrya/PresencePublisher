package org.ostrya.presencepublisher.preference.common;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.R;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractDynamicPreferenceCategorySupport {
    private final PreferenceManager preferenceManager;
    private final Fragment fragment;
    private final PreferenceCategory category;
    private final String listKey;
    private final String namespace;
    private final AdderFactory adderFactory;
    private final EntryFactory entryFactory;
    private Set<String> entryKeys = null;

    protected AbstractDynamicPreferenceCategorySupport(
            AbstractConfigurationFragment fragment,
            int categoryId,
            String listKey,
            String namespace,
            AdderFactory adderFactory,
            EntryFactory entryFactory) {
        this.preferenceManager = fragment.getPreferenceManager();
        Context context = preferenceManager.getContext();
        this.fragment = fragment;
        category = new MyPreferenceCategory(context, categoryId);
        category.setOrderingAsAdded(false);
        this.listKey = listKey;
        this.namespace = namespace;
        this.adderFactory = adderFactory;
        this.entryFactory = entryFactory;
        fragment.addListener(this::onPreferencesChanged);
    }

    protected PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    public PreferenceCategory getCategory() {
        return category;
    }

    public synchronized void initializeCategory() {
        Context context = preferenceManager.getContext();
        SharedPreferences preferences = preferenceManager.getSharedPreferences();
        entryKeys = preferences.getStringSet(listKey, Collections.emptySet());
        for (String key : entryKeys) {
            Preference entry =
                    entryFactory.create(context, namespace + key, key, preferences, fragment);
            // order alphabetically
            entry.setOrder(0);
            category.addPreference(entry);
        }
        Preference adderEntry = adderFactory.create(context, preferences, fragment);
        adderEntry.setIcon(R.drawable.baseline_playlist_add_24);
        adderEntry.setPersistent(false);
        adderEntry.setKey(adderEntry.getClass().getCanonicalName());
        // show last
        adderEntry.setOrder(Integer.MAX_VALUE - 1);
        category.addPreference(adderEntry);
    }

    private void onPreferencesChanged(SharedPreferences preferences, String key) {
        if (entryKeys == null) {
            // skipping event, not yet initialized
            return;
        }
        if (listKey.equals(key)) {
            updateValues(preferences);
        }
    }

    private synchronized void updateValues(SharedPreferences preferences) {
        Set<String> changedEntryKeys =
                Collections.unmodifiableSet(
                        preferences.getStringSet(listKey, Collections.emptySet()));
        Set<String> removed = new HashSet<>(entryKeys);
        removed.removeAll(changedEntryKeys);
        Set<String> added = new HashSet<>(changedEntryKeys);
        added.removeAll(entryKeys);
        entryKeys = changedEntryKeys;
        for (String add : added) {
            Preference entry =
                    entryFactory.create(
                            preferenceManager.getContext(),
                            namespace + add,
                            add,
                            preferences,
                            fragment);
            // order alphabetically
            entry.setOrder(0);
            category.addPreference(entry);
        }
        SharedPreferences.Editor editor = preferences.edit();
        for (String remove : removed) {
            category.removePreferenceRecursively(namespace + remove);
            editor.remove(namespace + remove);
        }
        editor.apply();
    }

    @FunctionalInterface
    public interface AdderFactory {
        Preference create(Context context, SharedPreferences preferences, Fragment fragment);
    }

    @FunctionalInterface
    public interface EntryFactory {
        Preference create(
                Context context,
                String key,
                String title,
                SharedPreferences preferences,
                Fragment fragment);
    }
}
