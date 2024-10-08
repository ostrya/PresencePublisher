package org.ostrya.presencepublisher.initialization;

import static org.ostrya.presencepublisher.preference.about.LocationConsentPreference.LOCATION_CONSENT;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.log.DatabaseLogger;

import java.util.Map;
import java.util.Queue;

public class EnsureLocationPermission
        extends AbstractChainedHandler<String[], Map<String, Boolean>> {

    protected EnsureLocationPermission(MainActivity activity, Queue<HandlerFactory> handlerChain) {
        super(activity, new ActivityResultContracts.RequestMultiplePermissions(), handlerChain);
    }

    @Override
    protected void doInitialize() {
        if ((activity.isLocationPermissionNeeded()
                        && ContextCompat.checkSelfPermission(
                                        activity, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED)
                // even if we don't need the permission, we effectively access location data, so we
                // must ask
                || !PreferenceManager.getDefaultSharedPreferences(activity)
                        .getBoolean(LOCATION_CONSENT, false)) {
            DatabaseLogger.i(TAG, "Location permission / consent not yet granted, asking user ...");
            FragmentManager fm = activity.getSupportFragmentManager();

            String allowPermissions;
            if (ContextCompat.checkSelfPermission(
                            activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                allowPermissions = activity.getString(R.string.location_permission_dialog_message);
            } else {
                allowPermissions = "";
            }

            ConfirmationDialogFragment fragment =
                    ConfirmationDialogFragment.getInstance(
                            this::onResult,
                            R.string.location_permission_dialog_title,
                            activity.getString(
                                    R.string.location_consent_dialog_summary,
                                    activity.getString(R.string.tab_about_title),
                                    activity.getString(R.string.privacy_title),
                                    activity.getString(R.string.location_consent_title),
                                    allowPermissions));
            fragment.show(fm, null);
        } else {
            finishInitialization();
        }
    }

    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            if (ContextCompat.checkSelfPermission(
                            activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                getLauncher()
                        .launch(
                                new String[] {
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                });
            } else {
                // can happen if the permission is already given but the consent was disabled before
                // in this case, we continue with initialization, and the next step will enable the
                // consent flag
                finishInitialization();
            }
        } else {
            DatabaseLogger.i(getName(), "User did not give consent. Stopping any further actions.");
            PreferenceManager.getDefaultSharedPreferences(parent)
                    .edit()
                    .putBoolean(LOCATION_CONSENT, false)
                    .apply();
            cancelInitialization();
        }
    }

    @Override
    protected void doHandleResult(Map<String, Boolean> result) {
        if (result != null
                && Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))) {
            DatabaseLogger.i(TAG, "Successfully granted location permission");
            finishInitialization();
        } else {
            FragmentManager fm = activity.getSupportFragmentManager();
            ConfirmationDialogFragment fragment =
                    ConfirmationDialogFragment.getInstance(
                            this::onRetrySetFineLocationResult,
                            R.string.location_permission_dialog_title,
                            activity.getString(
                                    R.string.location_permission_dialog_confirm_no_fine_location));
            fragment.setConfirmId(R.string.dialog_yes);
            fragment.setCancelId(R.string.dialog_no);
            fragment.show(fm, null);
        }
    }

    private void onRetrySetFineLocationResult(Activity parent, boolean ok) {
        if (!ok) {
            DatabaseLogger.w(TAG, "Location not granted, stopping initialization");
            PreferenceManager.getDefaultSharedPreferences(parent)
                    .edit()
                    .putBoolean(LOCATION_CONSENT, false)
                    .apply();
        } else {
            getLauncher()
                    .launch(
                            new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            });
        }
    }

    @Override
    protected String getName() {
        return "EnsureLocationPermission";
    }
}
