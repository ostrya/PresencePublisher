package org.ostrya.presencepublisher.preference.about;

import android.content.Context;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.LicenseDialogFragment;
import org.ostrya.presencepublisher.preference.common.ClickDummy;

public class BundledLicensesPreferenceDummy extends ClickDummy {
    public BundledLicensesPreferenceDummy(Context context, Fragment fragment) {
        super(
                context,
                R.drawable.baseline_text_snippet_24,
                R.string.bundled_licenses_title,
                R.string.bundled_licenses_summary,
                fragment);
    }

    @Override
    protected void onClick() {
        new LicenseDialogFragment().show(getParentFragmentManager(), null);
    }
}
