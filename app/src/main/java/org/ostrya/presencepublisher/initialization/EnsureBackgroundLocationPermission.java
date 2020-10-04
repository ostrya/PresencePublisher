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

import static org.ostrya.presencepublisher.Application.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE;

public class EnsureBackgroundLocationPermission extends AbstractChainedHandler {
    protected EnsureBackgroundLocationPermission(Queue<HandlerFactory> handlerChain) {
        super(BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE, handlerChain);
    }

    @Override
    protected void doInitialize(MainActivity context) {
        if (context.isLocationServiceNeeded()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            HyperLog.i(TAG, "Background location permission not yet granted, asking user ...");
            FragmentManager fm = context.getSupportFragmentManager();
            PackageManager pm = context.getPackageManager();

            ConfirmationDialogFragment fragment;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                fragment = ConfirmationDialogFragment.getInstance(this::onResult,
                        R.string.background_location_permission_dialog_title,
                        context.getString(R.string.background_location_permission_dialog_message, pm.getBackgroundPermissionOptionLabel()));
            } else {
                fragment = ConfirmationDialogFragment.getInstance(this::onResult,
                        R.string.background_location_permission_dialog_title,
                        R.string.location_permission_dialog_message);
            }
            fragment.show(fm, null);
        } else {
            finishInitialization(context);
        }
    }

    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            ActivityCompat.requestPermissions(parent,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    getRequestCode());
        }
    }

    @Override
    protected void doHandleResult(MainActivity context, int resultCode) {
        if (resultCode == PackageManager.PERMISSION_GRANTED) {
            HyperLog.i(TAG, "Successfully granted background location permission");
            finishInitialization(context);
        } else {
            HyperLog.w(TAG, "Expected result code " + PackageManager.PERMISSION_GRANTED + " but got " + resultCode);
        }
    }
}
