package org.ostrya.presencepublisher.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.hypertrack.hyperlog.HyperLog;
import org.ostrya.presencepublisher.R;

public class EditTextDialog extends DialogFragment {
    private static final String TAG = "EditTextDialog";

    private Callback callback;
    private int titleId;
    private String text;

    public static EditTextDialog getInstance(final Callback callback, int titleId) {
        EditTextDialog fragment = new EditTextDialog();
        fragment.setCallback(callback);
        fragment.setTitleId(titleId);
        return fragment;
    }

    public static EditTextDialog getInstance(final Callback callback, int titleId, String text) {
        EditTextDialog fragment = new EditTextDialog();
        fragment.setCallback(callback);
        fragment.setTitleId(titleId);
        fragment.setText(text);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_edit_text, null);

        EditText editText = view.findViewById(android.R.id.edit);
        if (editText != null) {
            editText.requestFocus();
            editText.setSelection(0);
            editText.setText(text);
        }

        builder.setTitle(titleId)
                .setView(view)
                .setPositiveButton(R.string.dialog_ok, (dialog, id) -> {
                    if (editText != null) {
                        callback.accept(editText.getText().toString());
                    } else {
                        HyperLog.e(TAG, "Unable to find edit text field");
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null);
        AlertDialog alertDialog = builder.create();

        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        return alertDialog;
    }

    private void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    private void setText(String text) {
        this.text = text;
    }

    public interface Callback {
        void accept(String newValue);
    }
}
