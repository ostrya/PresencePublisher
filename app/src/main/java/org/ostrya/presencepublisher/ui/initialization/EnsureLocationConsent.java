package org.ostrya.presencepublisher.ui.initialization;

import android.app.Activity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Queue;

import static org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference.LOCATION_CONSENT;

public class EnsureLocationConsent extends AbstractChainedHandler<Void, Void> {
    protected EnsureLocationConsent(Queue<HandlerFactory> handlerChain) {
        super(null, handlerChain);
    }

    @Override
    protected void doInitialize(MainActivity activity) {
        if (activity.isLocationServiceNeeded() && !PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(LOCATION_CONSENT, false)) {
            HyperLog.i(TAG, "Location consent not yet granted, asking user ...");
            FragmentManager fm = activity.getSupportFragmentManager();

            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(this::onResult,
                    R.string.location_consent_title,
                    activity.getString(R.string.location_consent_dialog_summary, activity.getString(R.string.tab_about_title), activity.getString(R.string.privacy_title), activity.getString(R.string.location_consent_title)));
            fragment.setConfirmId(R.string.dialog_accept);
            fragment.setCancelId(R.string.dialog_decline);
            fragment.show(fm, null);
        } else {
            finishInitialization();
        }
    }

    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            PreferenceManager.getDefaultSharedPreferences(parent).edit()
                    .putBoolean(LOCATION_CONSENT, true)
                    .apply();
            finishInitialization();
        } else {
            HyperLog.i(getName(), "User did not give consent. Stopping any further actions.");
            cancelInitialization();
        }
    }

    @Override
    protected void doHandleResult(Void result) {
        HyperLog.w(TAG, "Skipping unexpected result");
    }

    @Override
    protected String getName() {
        return "EnsureLocationConsent";
    }
}
