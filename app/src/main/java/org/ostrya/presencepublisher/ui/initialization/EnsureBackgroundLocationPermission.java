package org.ostrya.presencepublisher.ui.initialization;

import static org.ostrya.presencepublisher.ui.preference.about.LocationConsentPreference.LOCATION_CONSENT;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Queue;

public class EnsureBackgroundLocationPermission extends AbstractChainedHandler<String, Boolean> {
    protected EnsureBackgroundLocationPermission(
            MainActivity activity, Queue<HandlerFactory> handlerChain) {
        super(activity, new ActivityResultContracts.RequestPermission(), handlerChain);
    }

    @Override
    protected void doInitialize() {
        if (activity.isLocationServiceNeeded()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(
                                activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            DatabaseLogger.i(
                    TAG, "Background location permission not yet granted, asking user ...");
            FragmentManager fm = activity.getSupportFragmentManager();

            CharSequence optionName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PackageManager pm = activity.getPackageManager();
                optionName = pm.getBackgroundPermissionOptionLabel();
            } else {
                optionName =
                        activity.getString(R.string.background_location_permission_option_name);
            }
            ConfirmationDialogFragment fragment =
                    ConfirmationDialogFragment.getInstance(
                            this::onResult,
                            R.string.background_location_permission_dialog_title,
                            activity.getString(
                                    R.string.location_consent_dialog_summary,
                                    activity.getString(R.string.tab_about_title),
                                    activity.getString(R.string.privacy_title),
                                    activity.getString(R.string.location_consent_title),
                                    activity.getString(
                                            R.string.background_location_permission_dialog_message,
                                            optionName)));
            fragment.show(fm, null);
        } else {
            finishInitialization();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            PreferenceManager.getDefaultSharedPreferences(parent)
                    .edit()
                    .putBoolean(LOCATION_CONSENT, true)
                    .apply();
            getLauncher().launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
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
    protected void doHandleResult(Boolean result) {
        if (result) {
            DatabaseLogger.i(TAG, "Successfully granted background location permission");
            finishInitialization();
        } else {
            DatabaseLogger.w(TAG, "Background location not granted, stopping initialization");
        }
    }

    @Override
    protected String getName() {
        return "EnsureBackgroundLocationPermission";
    }
}
