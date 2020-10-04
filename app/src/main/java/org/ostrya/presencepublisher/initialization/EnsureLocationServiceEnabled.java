package org.ostrya.presencepublisher.initialization;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import androidx.fragment.app.FragmentManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Queue;

import static android.content.Context.LOCATION_SERVICE;
import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static org.ostrya.presencepublisher.Application.LOCATION_REQUEST_CODE;

public class EnsureLocationServiceEnabled extends AbstractChainedHandler {
    protected EnsureLocationServiceEnabled(Queue<HandlerFactory> handlerChain) {
        super(LOCATION_REQUEST_CODE, handlerChain);
    }

    @Override
    protected void doInitialize(MainActivity context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (context.isLocationServiceNeeded()
                && (locationManager == null || !(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)))) {
            HyperLog.i(TAG, "Location service not yet enabled, asking user ...");
            FragmentManager fm = context.getSupportFragmentManager();

            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(this::onResult,
                    R.string.location_dialog_title, R.string.location_dialog_message);
            fragment.show(fm, null);
        } else {
            finishInitialization(context);
        }
    }

    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            parent.startActivityForResult(new Intent(ACTION_LOCATION_SOURCE_SETTINGS), getRequestCode());
        }
    }

    @Override
    protected void doHandleResult(MainActivity context, int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            HyperLog.i(TAG, "Location service has successfully been started");
            finishInitialization(context);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            HyperLog.w(TAG, "User has cancelled starting location service");
        } else {
            HyperLog.e(TAG, "Custom result code " + resultCode);
        }
    }
}
