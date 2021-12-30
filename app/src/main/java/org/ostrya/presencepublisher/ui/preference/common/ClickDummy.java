package org.ostrya.presencepublisher.ui.preference.common;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;

public abstract class ClickDummy extends Preference {
    private final Fragment fragment;

    public ClickDummy(Context context, int iconId, int titleId, int summaryId, Fragment fragment) {
        super(context);
        setKey(getClass().getCanonicalName());
        setIcon(iconId);
        setTitle(titleId);
        setSummary(summaryId);
        setPersistent(false);
        this.fragment = fragment;
    }

    protected FragmentManager getParentFragmentManager() {
        return fragment.getParentFragmentManager();
    }
}
