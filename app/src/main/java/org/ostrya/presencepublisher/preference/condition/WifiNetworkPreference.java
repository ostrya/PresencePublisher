package org.ostrya.presencepublisher.preference.condition;

import static org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment.getInstance;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.preference.common.AbstractTextPreferenceEntry;
import org.ostrya.presencepublisher.preference.common.validation.NonEmptyStringValidator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WifiNetworkPreference extends AbstractTextPreferenceEntry {
    public static final String DEFAULT_CONTENT_ONLINE = "online";

    private final SharedPreferences preference;
    private final Fragment fragment;

    private final WifiNetwork network;

    public WifiNetworkPreference(
            Context context,
            String key,
            WifiNetwork network,
            SharedPreferences preference,
            Fragment fragment) {
        super(
                context,
                key,
                new NonEmptyStringValidator(),
                network.getName(),
                R.string.content_summary);
        this.preference = preference;
        this.fragment = fragment;
        this.network = network;
        setDefaultValue(DEFAULT_CONTENT_ONLINE);
    }

    @Override
    public boolean onLongClick(View v) {
        ConfirmationDialogFragment instance =
                getInstance(
                        this::deleteOnContinue,
                        R.string.remove_network_title,
                        R.string.remove_network_warning_message);
        instance.show(fragment.getParentFragmentManager(), null);
        return true;
    }

    private void deleteOnContinue(Activity unused, boolean ok) {
        if (ok) {
            Set<String> storedSsids =
                    new HashSet<>(
                            preference.getStringSet(
                                    WifiCategorySupport.SSID_LIST, Collections.emptySet()));
            storedSsids.remove(getTitle().toString());
            preference.edit().putStringSet(WifiCategorySupport.SSID_LIST, storedSsids).apply();
        }
    }

    @Override
    protected String getValue(@NonNull String text) {
        if (network.hasWildcard()) {
            return text + "\n" + getContext().getString(R.string.use_wildcard);
        } else {
            return text;
        }
    }
}
