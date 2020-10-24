package org.ostrya.presencepublisher.ui.preference.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.fragment.app.Fragment;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.ClickDummy;

public class SourceRepositoryPreference extends ClickDummy {

    public SourceRepositoryPreference(Context context, Fragment fragment) {
        super(context, android.R.drawable.ic_menu_view, R.string.source_repo_title, R.string.source_repo_summary, fragment);
    }

    @Override
    protected void onClick() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getContext().getString(R.string.source_repo_url)));
        getContext().startActivity(intent);
    }
}
