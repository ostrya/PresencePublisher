package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.dialog.CheckConnectionDialogFragment;

import static org.ostrya.presencepublisher.ui.dialog.CheckConnectionDialogFragment.getInstance;

public class CheckConnectionDummy extends Preference {
    public CheckConnectionDummy(Context context, Fragment fragment) {
        super(context);
        setTitle(R.string.check_connection_title);
        setSummary(R.string.check_connection_summary);
        setIcon(R.drawable.ic_notification);
        setOnPreferenceClickListener(prefs -> {
            CheckConnectionDialogFragment instance = getInstance(context);
            instance.show(fragment.requireFragmentManager(), null);
            return true;
        });
    }
}
