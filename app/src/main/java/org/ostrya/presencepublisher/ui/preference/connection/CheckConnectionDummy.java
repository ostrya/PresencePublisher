package org.ostrya.presencepublisher.ui.preference.connection;

import android.content.Context;
import androidx.fragment.app.Fragment;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.CheckConnectionDialogFragment;
import org.ostrya.presencepublisher.ui.preference.common.ClickDummy;

import static org.ostrya.presencepublisher.ui.dialog.CheckConnectionDialogFragment.getInstance;

public class CheckConnectionDummy extends ClickDummy {
    public CheckConnectionDummy(Context context, Fragment fragment) {
        super(context, R.drawable.ic_notification, R.string.check_connection_title, R.string.check_connection_summary, fragment);
    }

    @Override
    protected void onClick() {
        CheckConnectionDialogFragment instance = getInstance(getContext());
        instance.show(getParentFragmentManager(), null);
    }
}
