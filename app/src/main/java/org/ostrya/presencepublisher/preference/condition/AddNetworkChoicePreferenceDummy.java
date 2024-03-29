package org.ostrya.presencepublisher.preference.condition;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.EditNetworkDialog;
import org.ostrya.presencepublisher.mqtt.context.condition.network.NetworkService;
import org.ostrya.presencepublisher.preference.common.validation.NonEmptyStringValidator;
import org.ostrya.presencepublisher.preference.common.validation.Validator;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddNetworkChoicePreferenceDummy extends ListPreference {
    private final Validator validator = new NonEmptyStringValidator();
    private final String addNew;
    private final SharedPreferences sharedPreferences;
    private final NetworkService networkService;

    public AddNetworkChoicePreferenceDummy(
            Context context, SharedPreferences sharedPreferences, Fragment fragment) {
        super(context);
        this.addNew = context.getString(R.string.enter_network);
        this.sharedPreferences = sharedPreferences;
        this.networkService = new NetworkService(context);
        setTitle(R.string.add_network_title);
        setDialogTitle(R.string.add_network_title);
        setSummary(R.string.add_network_summary);
        setIcon(R.drawable.baseline_playlist_add_24);
        setOnPreferenceChangeListener(
                (prefs, newValue) -> {
                    if (addNew.equals(newValue)) {
                        EditNetworkDialog instance =
                                EditNetworkDialog.getInstance(
                                        this::onEditText, R.string.add_network_title);
                        instance.show(fragment.getParentFragmentManager(), null);
                    } else {
                        saveNewNetwork((String) newValue);
                    }
                    return false;
                });
    }

    @Override
    public CharSequence[] getEntries() {
        String[] entryValues =
                getCurrentEntries(
                        sharedPreferences.getStringSet(
                                WifiCategorySupport.SSID_LIST, Collections.emptySet()));
        setEntries(entryValues);
        setEntryValues(entryValues);
        return super.getEntries();
    }

    private void onEditText(String newValue, boolean useWildcards) {
        boolean result = this.validator.isValid(getContext(), null, newValue);
        if (!result) {
            String text = getContext().getString(R.string.toast_invalid_input);
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        } else {
            saveNewNetwork(WifiNetwork.toRawString(newValue, useWildcards));
        }
    }

    private void saveNewNetwork(String newValue) {
        Set<String> storedSsids =
                new HashSet<>(
                        sharedPreferences.getStringSet(
                                WifiCategorySupport.SSID_LIST, Collections.emptySet()));
        storedSsids.add(newValue);
        sharedPreferences.edit().putStringSet(WifiCategorySupport.SSID_LIST, storedSsids).apply();
    }

    private String[] getCurrentEntries(Set<String> storedSsids) {
        List<String> knownSsids = networkService.getKnownSsids();
        knownSsids.removeAll(storedSsids);
        knownSsids.add(addNew);
        return knownSsids.toArray(new String[0]);
    }
}
