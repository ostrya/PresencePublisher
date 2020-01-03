package org.ostrya.presencepublisher.ui.preference.condition;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceViewHolder;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.ui.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment.getInstance;
import static org.ostrya.presencepublisher.ui.preference.condition.AddNetworkChoicePreferenceDummy.SSID_LIST;
import static org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider.PreferenceType.STRING;

public class WifiNetworkPreference extends TextPreferenceBase implements View.OnLongClickListener {
    public static final String WIFI_CONTENT_PREFIX = "wifi.";
    public static final String DEFAULT_CONTENT_ONLINE = "online";

    private final SharedPreferences preference;
    private final Fragment fragment;

    public WifiNetworkPreference(Context context, String ssid, SharedPreferences preference, Fragment fragment) {
        super(context, WIFI_CONTENT_PREFIX + ssid, new RegexValidator(".+"), ssid);
        this.preference = preference;
        this.fragment = fragment;
        setDefaultValue(DEFAULT_CONTENT_ONLINE);
        // order alphabetically
        setOrder(0);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        ConfirmationDialogFragment instance = getInstance(this::deleteOnContinue, R.string.remove_network_title, R.string.remove_network_warning_message);
        instance.show(fragment.requireFragmentManager(), null);
        return true;
    }

    private void deleteOnContinue(boolean ok) {
        if (ok) {
            Set<String> storedSsids = new HashSet<>(preference.getStringSet(SSID_LIST, Collections.emptySet()));
            storedSsids.remove(getTitle().toString());
            preference.edit().putStringSet(SSID_LIST, storedSsids).apply();
        }
    }

    @Override
    protected void configureSummary() {
        setSummaryProvider(new ExplanationSummaryProvider<>(R.string.content_summary, STRING));
    }
}
