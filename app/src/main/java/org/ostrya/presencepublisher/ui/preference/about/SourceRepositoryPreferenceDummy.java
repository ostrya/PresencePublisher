package org.ostrya.presencepublisher.ui.preference.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.ClickDummy;

public class SourceRepositoryPreferenceDummy extends ClickDummy {

    public SourceRepositoryPreferenceDummy(Context context, Fragment fragment) {
        super(
                context,
                R.drawable.baseline_open_in_browser_24,
                R.string.source_repo_title,
                R.string.source_repo_summary,
                fragment);
    }

    @Override
    protected void onClick() {
        Intent intent =
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getContext().getString(R.string.source_repo_url)));
        getContext().startActivity(intent);
    }
}
