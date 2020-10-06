package org.ostrya.presencepublisher.ui.contract;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

public class DummyActivityResultLauncher<I> extends ActivityResultLauncher<I> {
    @Override
    public void launch(I input, @Nullable ActivityOptionsCompat activityOptionsCompat) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregister() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public ActivityResultContract<I, ?> getContract() {
        throw new UnsupportedOperationException();
    }
}
