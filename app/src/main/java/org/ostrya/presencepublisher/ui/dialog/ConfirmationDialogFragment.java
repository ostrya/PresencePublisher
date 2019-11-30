package org.ostrya.presencepublisher.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import org.ostrya.presencepublisher.R;

public class ConfirmationDialogFragment extends DialogFragment {
    private Callback callback;
    private int titleId;
    private int messageId;

    public static ConfirmationDialogFragment getInstance(final Callback callback, int titleId, int messageId) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        fragment.setCallback(callback);
        fragment.setTitleId(titleId);
        fragment.setMessageId(messageId);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.dialog_confirm, (dialog, id) -> callback.accept(true))
                .setNegativeButton(R.string.dialog_abort, (dialog, id) -> callback.accept(false));
        return builder.create();
    }

    private void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    private void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public interface Callback {
        void accept(boolean ok);
    }
}
