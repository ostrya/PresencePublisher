package org.ostrya.presencepublisher.ui.dialog;

import android.app.Activity;
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
    private String message;
    private int confirmId = R.string.dialog_ok;
    private int cancelId = R.string.dialog_cancel;

    public static ConfirmationDialogFragment getInstance(final Callback callback, int titleId, int messageId) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        fragment.setCallback(callback);
        fragment.setTitleId(titleId);
        fragment.setMessageId(messageId);
        return fragment;
    }

    public static ConfirmationDialogFragment getInstance(final Callback callback, int titleId, String message) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        fragment.setCallback(callback);
        fragment.setTitleId(titleId);
        fragment.setMessage(message);
        return fragment;
    }

    public void setConfirmId(int confirmId) {
        this.confirmId = confirmId;
    }

    public void setCancelId(int cancelId) {
        this.cancelId = cancelId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity parent = requireActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle(titleId)
                .setPositiveButton(confirmId, (dialog, id) -> callback.accept(parent, true))
                .setNegativeButton(cancelId, (dialog, id) -> callback.accept(parent, false));
        if (message != null) {
            builder.setMessage(message);
        } else {
            builder.setMessage(messageId);
        }
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

    private void setMessage(String message) {
        this.message = message;
    }

    public interface Callback {
        void accept(Activity parent, boolean ok);
    }
}
