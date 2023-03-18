package org.ostrya.presencepublisher.preference.common.validation;

import android.content.Context;

import androidx.annotation.Nullable;

public interface Validator {
    boolean isValid(Context context, @Nullable String key, String value);
}
