package org.ostrya.presencepublisher.preference.common;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceViewHolder;

import org.ostrya.presencepublisher.preference.common.validation.Validator;

public abstract class AbstractTextPreferenceEntry extends TextPreferenceBase
        implements View.OnLongClickListener {
    public AbstractTextPreferenceEntry(
            Context context, String key, Validator validator, CharSequence title, int summaryId) {
        super(context, key, validator, title, summaryId);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnLongClickListener(this);
    }
}
