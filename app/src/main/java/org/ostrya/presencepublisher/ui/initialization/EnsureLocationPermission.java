package org.ostrya.presencepublisher.ui.initialization;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Map;
import java.util.Queue;

public class EnsureLocationPermission extends AbstractChainedHandler<String[], Map<String, Boolean>> {
    protected EnsureLocationPermission(Queue<HandlerFactory> handlerChain) {
        super(new ActivityResultContracts.RequestMultiplePermissions(), handlerChain);
    }

    @Override
    protected void doInitialize(MainActivity activity) {
        if (activity.isLocationServiceNeeded()
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            HyperLog.i(TAG, "Location permission not yet granted, asking user ...");
            FragmentManager fm = activity.getSupportFragmentManager();

            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(this::onResult,
                    R.string.location_permission_dialog_title, R.string.location_permission_dialog_message);
            fragment.show(fm, null);
        } else {
            finishInitialization();
        }
    }

    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                getLauncher().launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION});
            } else {
                getLauncher().launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
            }
        }
    }

    @Override
    protected void doHandleResult(Map<String, Boolean> resultMap) {
        Boolean result = resultMap.get(Manifest.permission.ACCESS_FINE_LOCATION);
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
