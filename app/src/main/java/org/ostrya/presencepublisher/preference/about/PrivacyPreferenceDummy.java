package org.ostrya.presencepublisher.preference.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.ClickDummy;

public class PrivacyPreferenceDummy extends ClickDummy {
    public PrivacyPreferenceDummy(Context context, Fragment fragment) {
        super(
                context,
                R.drawable.baseline_read_more_24,
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
