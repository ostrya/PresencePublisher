package org.ostrya.presencepublisher.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.ostrya.presencepublisher.R;

public class ScrollableMessageFragment extends DialogFragment {
    private CharSequence message;

    public static ScrollableMessageFragment getInstance(CharSequence message) {
        ScrollableMessageFragment fragment = new ScrollableMessageFragment();
        fragment.setMessage(message);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup root = requireActivity().findViewById(android.R.id.content);
        View view = inflater.inflate(R.layout.fragment_scrollable_message, root, false);

        TextView messagesView = view.findViewById(R.id.messages);
        if (messagesView != null) {
            messagesView.setText(message);
        }

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.message_format_help_title)
                .setView(view)
                .setNeutralButton(R.string.dialog_close, null)
                .create();
    }

    private void setMessage(CharSequence message) {
        this.message = message;
    }
}
