package org.ostrya.presencepublisher.ui.initialization;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Queue;

import static org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference.LOCATION_CONSENT;

public class EnsureLocationPermission extends AbstractChainedHandler<String, Boolean> {
    protected EnsureLocationPermission(MainActivity activity, Queue<HandlerFactory> handlerChain) {
        super(activity, new ActivityResultContracts.RequestPermission(), handlerChain);
    }

    @Override
    protected void doInitialize() {
        if (activity.isLocationServiceNeeded()
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            HyperLog.i(TAG, "Location permission not yet granted, asking user ...");
            FragmentManager fm = activity.getSupportFragmentManager();

            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(
                    this::onResult,
                    R.string.location_permission_dialog_title,
                    activity.getString(R.string.location_consent_dialog_summary,
                            activity.getString(R.string.tab_about_title),
                            activity.getString(R.string.privacy_title),
                            activity.getString(R.string.location_consent_title),
                            activity.getString(R.string.location_permission_dialog_message)));
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
            getLauncher().launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            HyperLog.i(getName(), "User did not give consent. Stopping any further actions.");
            PreferenceManager.getDefaultSharedPreferences(parent).edit()
                    .putBoolean(LOCATION_CONSENT, false)
                    .apply();
            cancelInitialization();
        }
    }

    @Override
    protected void doHandleResult(Boolean result) {
        if (result != null && result) {
            HyperLog.i(TAG, "Successfully granted location permission");
            finishInitialization();
        } else {
            HyperLog.w(TAG, "Location not granted, stopping initialization");
        }
    }

    @Override
    protected String getName() {
        return "EnsureLocationPermission";
    }
}
