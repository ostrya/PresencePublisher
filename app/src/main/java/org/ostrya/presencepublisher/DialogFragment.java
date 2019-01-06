package org.ostrya.presencepublisher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialogFragment extends androidx.fragment.app.DialogFragment {
    private Callback callback;
    private int titleId;
    private int messageId;

    static DialogFragment getInstance(final Callback callback, int titleId, int messageId) {
        DialogFragment fragment = new DialogFragment();
        fragment.setCallback(callback);
        fragment.setTitleId(titleId);
        fragment.setMessageId(messageId);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.dialog_continue, (dialog, id) -> callback.accept(true))
                .setNegativeButton(R.string.dialog_skip, (dialog, id) -> callback.accept(false));
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
