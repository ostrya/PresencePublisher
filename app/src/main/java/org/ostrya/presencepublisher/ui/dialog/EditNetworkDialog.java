package org.ostrya.presencepublisher.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;

public class EditNetworkDialog extends DialogFragment {
    private static final String TAG = "EditNetworkDialog";

    private Callback callback;
    private int titleId;

    public static EditNetworkDialog getInstance(final Callback callback, int titleId) {
        EditNetworkDialog fragment = new EditNetworkDialog();
        fragment.setCallback(callback);
        fragment.setTitleId(titleId);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = LayoutInflater.from(getContext());
        ViewGroup root = requireActivity().findViewById(android.R.id.content);
        View view = inflater.inflate(R.layout.dialog_edit_network, root, false);

        EditText editText = view.findViewById(android.R.id.edit);
        if (editText != null) {
            editText.requestFocus();
            editText.setSelection(0);
        }

        SwitchMaterial useWildcards = view.findViewById(R.id.useWildcards);

        builder.setTitle(titleId)
                .setView(view)
                .setPositiveButton(
                        R.string.dialog_ok,
                        (dialog, id) -> {
                            if (editText != null) {
                                callback.accept(
                                        editText.getText().toString(),
                                        useWildcards != null && useWildcards.isChecked());
                            } else {
                                DatabaseLogger.e(TAG, "Unable to find edit text field");
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

    public interface Callback {
        void accept(String newValue, boolean useWildcards);
    }
}
