package org.ostrya.presencepublisher.initialization;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Queue;

import static org.ostrya.presencepublisher.Application.LOCATION_PERMISSION_REQUEST_CODE;

public class EnsureLocationPermission extends AbstractChainedHandler {
    protected EnsureLocationPermission(Queue<HandlerFactory> handlerChain) {
        super(LOCATION_PERMISSION_REQUEST_CODE, handlerChain);
    }

    @Override
    protected void doInitialize(MainActivity context) {
        if (context.isLocationServiceNeeded()
                && ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            HyperLog.i(TAG, "Location permission not yet granted, asking user ...");
            FragmentManager fm = context.getSupportFragmentManager();

            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(this::onResult,
                    R.string.location_permission_dialog_title, R.string.location_permission_dialog_message);
            fragment.show(fm, null);
        } else {
            finishInitialization(context);
        }
    }

    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(parent,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        getRequestCode());
            } else {
                ActivityCompat.requestPermissions(parent,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        getRequestCode());
            }
        }
    }

    @Override
    protected void doHandleResult(MainActivity context, int resultCode) {
        if (resultCode == PackageManager.PERMISSION_GRANTED) {
            HyperLog.i(TAG, "Successfully granted location permission");
            finishInitialization(context);
        } else {
            HyperLog.w(TAG, "Expected result code " + PackageManager.PERMISSION_GRANTED + " but got " + resultCode);
        }
    }
}
