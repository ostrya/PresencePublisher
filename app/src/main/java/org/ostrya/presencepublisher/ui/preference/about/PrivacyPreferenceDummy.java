package org.ostrya.presencepublisher.ui.preference.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.ClickDummy;

public class PrivacyPreferenceDummy extends ClickDummy {
    public PrivacyPreferenceDummy(Context context, Fragment fragment) {
        super(
                context,
                android.R.drawable.ic_menu_more,
                R.string.privacy_title,
                R.string.privacy_summary,
                fragment);
    }

    @Override
    protected void onClick() {
        Intent intent =
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getContext().getString(R.string.privacy_url)));
        getContext().startActivity(intent);
    }
}
