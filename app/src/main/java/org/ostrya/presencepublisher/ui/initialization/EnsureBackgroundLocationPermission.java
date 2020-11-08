package org.ostrya.presencepublisher.ui.initialization;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.ConfirmationDialogFragment;

import java.util.Queue;

public class EnsureBackgroundLocationPermission extends AbstractChainedHandler<String, Boolean> {
    protected EnsureBackgroundLocationPermission(Queue<HandlerFactory> handlerChain) {
        super(new ActivityResultContracts.RequestPermission(), handlerChain);
    }

    @Override
    protected void doInitialize(MainActivity activity) {
        if (activity.isLocationServiceNeeded()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            HyperLog.i(TAG, "Background location permission not yet granted, asking user ...");
            FragmentManager fm = activity.getSupportFragmentManager();

            CharSequence optionName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PackageManager pm = activity.getPackageManager();
                optionName = pm.getBackgroundPermissionOptionLabel();
            } else {
                optionName = activity.getString(R.string.background_location_permission_option_name);
            }
            ConfirmationDialogFragment fragment = ConfirmationDialogFragment.getInstance(this::onResult,
                    R.string.background_location_permission_dialog_title,
                    activity.getString(R.string.background_location_permission_dialog_message, optionName));
            fragment.show(fm, null);
        } else {
            finishInitialization();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            getLauncher().launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
    }

    @Override
    protected void doHandleResult(Boolean result) {
        if (result) {
            HyperLog.i(TAG, "Successfully granted background location permission");
            finishInitialization();
        } else {
            HyperLog.w(TAG, "Background location not granted, stopping initialization");
        }
    }

    @Override
    protected String getName() {
        return "EnsureBackgroundLocationPermission";
    }
}
