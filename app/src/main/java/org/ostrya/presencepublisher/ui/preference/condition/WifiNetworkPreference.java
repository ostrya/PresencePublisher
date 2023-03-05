package org.ostrya.presencepublisher.ui.preference.condition;

import static org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment.getInstance;
import static org.ostrya.presencepublisher.ui.preference.condition.WifiCategorySupport.SSID_LIST;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.ui.preference.common.AbstractTextPreferenceEntry;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;
import org.ostrya.presencepublisher.ui.util.NonEmptyStringValidator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WifiNetworkPreference extends AbstractTextPreferenceEntry {
    public static final String DEFAULT_CONTENT_ONLINE = "online";

    private final SharedPreferences preference;
    private final Fragment fragment;

    public WifiNetworkPreference(
            Context context,
            String key,
            String ssid,
            SharedPreferences preference,
            Fragment fragment) {
        super(context, key, new NonEmptyStringValidator(), ssid);
        this.preference = preference;
        this.fragment = fragment;
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
                    new HashSet<>(preference.getStringSet(SSID_LIST, Collections.emptySet()));
            storedSsids.remove(getTitle().toString());
            preference.edit().putStringSet(SSID_LIST, storedSsids).apply();
        }
    }

    @Override
    protected void configureSummary() {
        setSummaryProvider(new ExplanationSummaryProvider<>(R.string.content_summary));
    }
}
