package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;
import android.view.View;

import androidx.preference.PreferenceViewHolder;

import org.ostrya.presencepublisher.ui.util.Validator;

public abstract class AbstractTextPreferenceEntry extends TextPreferenceBase
        implements View.OnLongClickListener {
    public AbstractTextPreferenceEntry(
            Context context, String key, Validator validator, CharSequence title, int summaryId) {
        super(context, key, validator, title, summaryId);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnLongClickListener(this);
    }
}
