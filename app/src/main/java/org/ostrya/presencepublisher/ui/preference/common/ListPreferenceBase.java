package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.ostrya.presencepublisher.R;

public class ListPreferenceBase extends ListPreference
        implements Preference.SummaryProvider<ListPreferenceBase> {
    private final int summaryId;

    protected ListPreferenceBase(
            Context context, String key, int titleId, String defaultValue, int summaryId) {
        super(context);
        this.summaryId = summaryId;
        setKey(key);
        setIconSpaceReserved(false);
        setTitle(titleId);
        setDialogTitle(titleId);
        setDefaultValue(defaultValue);
        setSummaryProvider(this);
    }

    @Nullable
    @Override
    public CharSequence provideSummary(@NonNull ListPreferenceBase preference) {
        CharSequence entry = preference.getEntry();
        Context context = preference.getContext();
        if (TextUtils.isEmpty(entry)) {
            return context.getString(summaryId, context.getString(R.string.value_undefined));
        } else {
            return context.getString(summaryId, preference.getValue(entry));
        }
    }

    protected String getValue(@NonNull CharSequence text) {
        return text.toString();
    }
}
