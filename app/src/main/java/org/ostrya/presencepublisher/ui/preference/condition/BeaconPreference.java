package org.ostrya.presencepublisher.ui.preference.condition;

import static org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment.getInstance;
import static org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider.PreferenceType.STRING;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceViewHolder;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.beacon.PresenceBeaconManager;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.ui.preference.common.TextPreferenceBase;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;
import org.ostrya.presencepublisher.ui.util.RegexValidator;

public class BeaconPreference extends TextPreferenceBase implements View.OnLongClickListener {
    public static final String DEFAULT_CONTENT_ONLINE = "online";

    private final Fragment fragment;
    private final String beaconId;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public BeaconPreference(Context context, String key, String beaconId, Fragment fragment) {
        super(context, key, new RegexValidator(".+"), beaconId);
        this.beaconId = beaconId;
        this.fragment = fragment;
        setDefaultValue(DEFAULT_CONTENT_ONLINE);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        ConfirmationDialogFragment instance =
                getInstance(
                        this::deleteOnContinue,
                        R.string.remove_region_title,
                        R.string.remove_region_warning_message);
        instance.show(fragment.getParentFragmentManager(), null);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void deleteOnContinue(Activity unused, boolean ok) {
        if (ok) {
            PresenceBeaconManager.getInstance().removeBeacon(getContext(), beaconId);
        }
    }

    @Override
    protected void configureSummary() {
        setSummaryProvider(new ExplanationSummaryProvider<>(R.string.content_summary, STRING));
    }
}
