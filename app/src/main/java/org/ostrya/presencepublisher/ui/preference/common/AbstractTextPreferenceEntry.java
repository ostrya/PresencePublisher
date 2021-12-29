package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;
import android.view.View;

import androidx.preference.PreferenceViewHolder;

import org.ostrya.presencepublisher.ui.util.Validator;

public abstract class AbstractTextPreferenceEntry extends TextPreferenceBase
        implements View.OnLongClickListener {
    public AbstractTextPreferenceEntry(
            Context context, String key, Validator validator, int titleId) {
        super(context, key, validator, titleId);
    }

    public AbstractTextPreferenceEntry(
            Context context, String key, Validator validator, CharSequence title) {
        super(context, key, validator, title);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnLongClickListener(this);
    }
}
