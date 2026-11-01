package org.ostrya.presencepublisher.preference.about;

import android.app.Activity;
import android.content.Context;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.preference.common.ClickDummy;

public class RestorePreferenceDummy extends ClickDummy {
    private final ActivityResultLauncher<String[]> restoreLauncher;

    public RestorePreferenceDummy(
            Context context, ActivityResultLauncher<String[]> restoreLauncher, Fragment fragment) {
        super(
                context,
                R.drawable.baseline_settings_backup_restore_24,
                R.string.restore_title,
                R.string.restore_summary,
                fragment);
        this.restoreLauncher = restoreLauncher;
    }

    @Override
    protected void onClick() {
        ConfirmationDialogFragment.getInstance(
                        this::onConfirm, R.string.backup_title, R.string.backup_confirmation)
                .show(getParentFragmentManager(), null);
    }

    private void onConfirm(Activity parent, boolean ok) {
        if (ok) {
            restoreLauncher.launch(new String[] {"application/json"});
        }
    }
}
