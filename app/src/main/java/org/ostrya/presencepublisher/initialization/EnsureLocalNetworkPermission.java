package org.ostrya.presencepublisher.initialization;

import static org.ostrya.presencepublisher.preference.connection.HostPreference.HOST;

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
import org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.LocalAddressChecker;

import java.util.Queue;

class EnsureLocalNetworkPermission extends AbstractChainedHandler<String, Boolean> {
    protected EnsureLocalNetworkPermission(
            MainActivity activity, Queue<HandlerFactory> handlerChain) {
        super(activity, new ActivityResultContracts.RequestPermission(), handlerChain);
    }

    @Override
    protected void doInitialize() {
        String host = PreferenceManager.getDefaultSharedPreferences(activity).getString(HOST, null);
        // skip permission check if no host is configured or not a local address or < SDK 37
        if (host != null
                && LocalAddressChecker.isLocalAddress(host)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN
                && ContextCompat.checkSelfPermission(
                                activity, Manifest.permission.ACCESS_LOCAL_NETWORK)
                        != PackageManager.PERMISSION_GRANTED) {
            DatabaseLogger.d(
                    getName(), "Local network permission not yet granted, asking user ...");
            FragmentManager fm = activity.getSupportFragmentManager();

            ConfirmationDialogFragment fragment =
                    ConfirmationDialogFragment.getInstance(
                            this::onResult,
                            R.string.local_network_permission_dialog_title,
                            R.string.local_network_permission_dialog_message);
            fragment.show(fm, null);
        } else {
            finishInitialization();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.CINNAMON_BUN)
    private void onResult(Activity parent, boolean ok) {
        if (ok) {
            getLauncher().launch(Manifest.permission.ACCESS_LOCAL_NETWORK);
        } else {
            // let's hope for the best - maybe the address is not local after all?
            finishInitialization();
        }
    }

    @Override
    protected void doHandleResult(Boolean result) {
        DatabaseLogger.d(
                getName(),
                "User "
                        + (Boolean.TRUE.equals(result) ? "granted" : "did not grant")
                        + " permission, continuing initialization");
        finishInitialization();
    }

    @Override
    protected String getName() {
        return "EnsureLocalNetworkPermission";
    }
}
