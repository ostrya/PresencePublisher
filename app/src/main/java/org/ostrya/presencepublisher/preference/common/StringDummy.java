package org.ostrya.presencepublisher.preference.common;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class StringDummy extends Preference {
    public StringDummy(Context context, int summaryId) {
        super(context);
        setSummary(summaryId);
        setIconSpaceReserved(false);
    }

    public StringDummy(Context context, CharSequence summary) {
        super(context);
        setSummary(summary);
        setIconSpaceReserved(false);
    }

    public StringDummy(Context context, int titleId, CharSequence summary) {
        super(context);
        setTitle(titleId);
        setSummary(summary);
        setIconSpaceReserved(false);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        if (summaryView != null) {
            summaryView.setMaxLines(Integer.MAX_VALUE);
        }
    }
}
