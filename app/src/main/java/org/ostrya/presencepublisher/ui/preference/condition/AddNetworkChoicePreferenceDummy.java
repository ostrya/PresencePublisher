package org.ostrya.presencepublisher.ui.preference.condition;

import static org.ostrya.presencepublisher.ui.preference.condition.WifiCategorySupport.SSID_LIST;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.message.wifi.SsidUtil;
import org.ostrya.presencepublisher.ui.dialog.EditTextDialog;
import org.ostrya.presencepublisher.ui.util.RegexValidator;
import org.ostrya.presencepublisher.ui.util.Validator;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddNetworkChoicePreferenceDummy extends ListPreference {
    private final Validator validator = new RegexValidator(".+");
    private final String addNew;
    private final SharedPreferences sharedPreferences;

    public AddNetworkChoicePreferenceDummy(
            Context context, SharedPreferences sharedPreferences, Fragment fragment) {
        super(context);
        this.addNew = context.getString(R.string.enter_network);
        this.sharedPreferences = sharedPreferences;
        setTitle(R.string.add_network_title);
        setDialogTitle(R.string.add_network_title);
        setSummary(R.string.add_network_summary);
        setOnPreferenceChangeListener(
                (prefs, newValue) -> {
                    if (addNew.equals(newValue)) {
                        EditTextDialog instance =
                                EditTextDialog.getInstance(
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
                        sharedPreferences.getStringSet(SSID_LIST, Collections.emptySet()));
        setEntries(entryValues);
        setEntryValues(entryValues);
        return super.getEntries();
    }

    private void onEditText(String newValue) {
        boolean result = this.validator.isValid(newValue);
        if (!result) {
            String text = getContext().getString(R.string.toast_invalid_input);
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        } else if (!newValue.isEmpty()) {
            saveNewNetwork(newValue);
        }
    }

    private void saveNewNetwork(String newValue) {
        Set<String> storedSsids =
                new HashSet<>(sharedPreferences.getStringSet(SSID_LIST, Collections.emptySet()));
        storedSsids.add(newValue);
        sharedPreferences.edit().putStringSet(SSID_LIST, storedSsids).apply();
    }

    private String[] getCurrentEntries(Set<String> storedSsids) {
        List<String> knownSsids = SsidUtil.getKnownSsids(getContext());
        knownSsids.removeAll(storedSsids);
        knownSsids.add(addNew);
        return knownSsids.toArray(new String[0]);
    }
}
