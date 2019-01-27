package org.ostrya.presencepublisher.ui.util;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.Preference;

public class CustomPreference extends Preference {
    public CustomPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreference(Context context) {
        super(context);
    }

    public String getValue(String defaultReturnValue) {
        return super.getPersistedString(defaultReturnValue);
    }

    public void setValue(String text) {
        final boolean wasBlocking = shouldDisableDependents();

        persistString(text);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }

        notifyChanged();
    }
}
