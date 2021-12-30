package org.ostrya.presencepublisher.ui.preference.about;

import android.content.Context;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.LicenseDialogFragment;
import org.ostrya.presencepublisher.ui.preference.common.ClickDummy;

public class BundledLicensesPreferenceDummy extends ClickDummy {
    public BundledLicensesPreferenceDummy(Context context, Fragment fragment) {
        super(
                context,
                android.R.drawable.ic_menu_info_details,
                R.string.bundled_licenses_title,
                R.string.bundled_licenses_summary,
                fragment);
    }

    @Override
    protected void onClick() {
        new LicenseDialogFragment().show(getParentFragmentManager(), null);
    }
}
