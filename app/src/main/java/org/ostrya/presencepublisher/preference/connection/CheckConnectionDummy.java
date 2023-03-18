package org.ostrya.presencepublisher.preference.connection;

import static org.ostrya.presencepublisher.dialog.CheckConnectionDialogFragment.getInstance;

import android.content.Context;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.CheckConnectionDialogFragment;
import org.ostrya.presencepublisher.preference.common.ClickDummy;

public class CheckConnectionDummy extends ClickDummy {
    public CheckConnectionDummy(Context context, Fragment fragment) {
        super(
                context,
                R.drawable.ic_notification,
                R.string.check_connection_title,
                R.string.check_connection_summary,
                fragment);
    }

    @Override
    protected void onClick() {
        CheckConnectionDialogFragment instance = getInstance(getContext());
        instance.show(getParentFragmentManager(), null);
    }
}
