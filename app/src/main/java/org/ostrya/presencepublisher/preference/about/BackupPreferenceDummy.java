package org.ostrya.presencepublisher.preference.about;

import android.app.Activity;
import android.content.Context;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.ConfirmationDialogFragment;
import org.ostrya.presencepublisher.preference.common.ClickDummy;

public class BackupPreferenceDummy extends ClickDummy {
    private final ActivityResultLauncher<String> backupLauncher;

    public BackupPreferenceDummy(
            Context context, ActivityResultLauncher<String> backupLauncher, Fragment fragment) {
        super(
                context,
                R.drawable.baseline_import_export_24,
                R.string.backup_title,
                R.string.backup_summary,
                fragment);
        this.backupLauncher = backupLauncher;
    }

    @Override
    protected void onClick() {
        ConfirmationDialogFragment.getInstance(
                        this::onConfirm, R.string.backup_title, R.string.backup_confirmation)
                .show(getParentFragmentManager(), null);
    }

    private void onConfirm(Activity parent, boolean ok) {
        if (ok) {
            backupLauncher.launch("settings_" + System.currentTimeMillis() + ".json");
        }
    }
}
