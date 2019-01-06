package org.ostrya.presencepublisher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PermissionsDialogFragment extends DialogFragment {
    private Callback callback;

    static PermissionsDialogFragment getInstance(final Callback callback) {
        PermissionsDialogFragment fragment = new PermissionsDialogFragment();
        fragment.setCallback(callback);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_permission)
                .setPositiveButton(R.string.dialog_continue, (dialog, id) -> callback.accept(true))
                .setNegativeButton(R.string.dialog_skip, (dialog, id) -> callback.accept(false));
        return builder.create();
    }

    private void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void accept(boolean ok);
    }
}
