package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class StringDummy extends Preference {
    public StringDummy(Context context, int summaryId) {
        super(context);
        setSummary(summaryId);
        setIconSpaceReserved(false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        if (summaryView != null) {
            summaryView.setMaxLines(Integer.MAX_VALUE);
        }
    }

    public StringDummy(Context context, int titleId, int summaryId) {
        super(context);
        setTitle(titleId);
        setSummary(summaryId);
        setIconSpaceReserved(false);
    }

    public StringDummy(Context context, int titleId, CharSequence summary) {
        super(context);
        setTitle(titleId);
        setSummary(summary);
        setIconSpaceReserved(false);
    }
}
