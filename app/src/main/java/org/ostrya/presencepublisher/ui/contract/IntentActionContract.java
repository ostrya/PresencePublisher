package org.ostrya.presencepublisher.ui.contract;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.ostrya.presencepublisher.log.DatabaseLogger;

public class IntentActionContract extends ActivityResultContract<String, Boolean> {
    private static final String TAG = "IntentActionContract";
    private final UriFactory uriFactory;
    @Nullable private String action = null;

    public IntentActionContract() {
        this.uriFactory = context -> null;
    }

    public IntentActionContract(UriFactory uriFactory) {
        this.uriFactory = uriFactory;
    }

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, String action) {
        this.action = action;
        return new Intent(action, uriFactory.create(context));
    }

    @Override
    public Boolean parseResult(int resultCode, @Nullable Intent intent) {
        DatabaseLogger.d(TAG, "Received result code " + resultCode + " for action " + action);
        return resultCode == Activity.RESULT_OK;
    }

    @FunctionalInterface
    public interface UriFactory {
        @Nullable
        Uri create(Context context);
    }
}
