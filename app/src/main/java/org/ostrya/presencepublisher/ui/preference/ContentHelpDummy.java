package org.ostrya.presencepublisher.ui.preference;

import android.content.Context;
import androidx.preference.Preference;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider;

import static org.ostrya.presencepublisher.ui.util.ExplanationSummaryProvider.PreferenceType.STRING;

public class ContentHelpDummy extends Preference {
    public ContentHelpDummy(Context context) {
        super(context);
        setSummaryProvider(new ExplanationSummaryProvider(R.string.content_help_summary, STRING));
        setIconSpaceReserved(false);
    }
}
