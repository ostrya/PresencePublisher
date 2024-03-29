package org.ostrya.presencepublisher.preference.condition;

import static org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment.getInstance;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.mqtt.context.condition.beacon.PresenceBeaconManager;
import org.ostrya.presencepublisher.preference.common.AbstractTextPreferenceEntry;
import org.ostrya.presencepublisher.preference.common.validation.NonEmptyStringValidator;

public class BeaconPreference extends AbstractTextPreferenceEntry {
    public static final String DEFAULT_CONTENT_ONLINE = "online";

    private final Fragment fragment;
    private final String beaconId;

    public BeaconPreference(Context context, String key, String beaconId, Fragment fragment) {
        super(context, key, new NonEmptyStringValidator(), beaconId, R.string.content_summary);
        this.beaconId = beaconId;
        this.fragment = fragment;
        setDefaultValue(DEFAULT_CONTENT_ONLINE);
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

    private void deleteOnContinue(Activity unused, boolean ok) {
        if (ok) {
            PresenceBeaconManager.getInstance().removeBeacon(getContext(), beaconId);
        }
    }
}
