package org.ostrya.presencepublisher.ui.initialization;

import static android.content.Context.LOCATION_SERVICE;
import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

import android.app.Activity;
import android.location.LocationManager;

import androidx.fragment.app.FragmentManager;

import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.ui.contract.IntentActionContract;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Queue;

public class EnsureLocationServiceEnabled extends AbstractChainedHandler<String, Boolean> {
    protected EnsureLocationServiceEnabled(
            MainActivity activity, Queue<HandlerFactory> handlerChain) {
        super(activity, new IntentActionContract(), handlerChain);
    }

    @Override
    protected void doInitialize() {
        LocationManager locationManager =
                (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        if (activity.isLocationPermissionNeeded()
                && (locationManager == null
                        || !(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                                || locationManager.isProviderEnabled(
                                        LocationManager.NETWORK_PROVIDER)))) {
            DatabaseLogger.i(TAG, "Location service not yet enabled, asking user ...");
            FragmentManager fm = activity.getSupportFragmentManager();

            ConfirmationDialogFragment fragment =
                    ConfirmationDialogFragment.getInstance(
                            this::onResult,
                            R.string.location_dialog_title,
                            R.string.location_dialog_message);
            fragment.show(fm, null);
        } else {
            finishInitialization();
        }
    }

    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            getLauncher().launch(ACTION_LOCATION_SOURCE_SETTINGS);
        }
    }

    @Override
    protected void doHandleResult(Boolean result) {
        if (result) {
            DatabaseLogger.i(TAG, "Location service has successfully been started");
            finishInitialization();
        } else {
            DatabaseLogger.w(TAG, "User has cancelled starting location service");
        }
    }

    @Override
    protected String getName() {
        return "EnsureLocationServiceEnabled";
    }
}
