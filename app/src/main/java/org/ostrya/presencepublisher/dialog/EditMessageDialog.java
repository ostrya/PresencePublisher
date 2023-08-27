package org.ostrya.presencepublisher.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.message.MessageItem;
import org.ostrya.presencepublisher.preference.message.MessageConfiguration;

import java.util.ArrayList;
import java.util.List;

public class EditMessageDialog extends DialogFragment {
    private static final String TAG = "EditMessageDialog";

    private Callback callback;
    @Nullable private String name;
    @Nullable private String topic;

    @NonNull
    private final boolean[] selectedItems = new boolean[MessageItem.settingValues().length];

    public static EditMessageDialog getInstance(
            final Callback callback,
            @Nullable String name,
            @Nullable MessageConfiguration messageConfiguration) {
        EditMessageDialog fragment = new EditMessageDialog();
        fragment.setCallback(callback);
        if (name != null && messageConfiguration != null) {
            fragment.setName(name);
            fragment.setTopic(messageConfiguration.getTopic());
            MessageItem[] messageItems = MessageItem.settingValues();
            for (int i = 0; i < messageItems.length; i++) {
                MessageItem item = messageItems[i];
                if (messageConfiguration.getItems().contains(item)) {
                    fragment.setSelectedItem(i);
                }
            }
        }
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup root = requireActivity().findViewById(android.R.id.content);
        View view = inflater.inflate(R.layout.dialog_edit_message, root, false);

        TextInputEditText editName = view.findViewById(R.id.edit_name);
        if (editName != null && name != null) {
            editName.setText(name);
        }
        TextInputEditText editTopic = view.findViewById(R.id.edit_topic);
        if (editTopic != null && topic != null) {
            editTopic.setText(topic);
        }

        builder.setView(view)
                .setTitle(R.string.edit_message_title_1)
                .setPositiveButton(
                        R.string.dialog_next,
                        (dialog, id) -> {
                            if (editName != null && editTopic != null) {
                                Editable nameText = editName.getText();
                                String newName;
                                if (nameText == null) {
                                    newName = null;
                                } else {
                                    newName = nameText.toString();
                                }

                                String newTopic;
                                Editable topicText = editTopic.getText();
                                if (topicText == null) {
                                    newTopic = null;
                                } else {
                                    newTopic = topicText.toString();
                                }

                                if (newTopic != null) {
                                    // As the list of message contents is quite long, it is split
                                    // to a second selection dialog. This also removes the need for
                                    // fiddling with the auto-generated dialog not showing the soft
                                    // keyboard properly.
                                    showDialogPart2(context, newName, newTopic);
                                } else {
                                    callback.accept(newName, null);
                                }
                            } else {
                                DatabaseLogger.e(TAG, "Unable to find edit text fields");
                            }
                        })
                .setNegativeButton(R.string.dialog_cancel, null);

        return builder.create();
    }

    private void showDialogPart2(Context context, String newName, String newTopic) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.edit_message_title_2)
                .setMultiChoiceItems(
                        MessageItem.settingDescriptions(),
                        selectedItems,
                        (dialog, which, isChecked) -> {
                            // it looks like the selectedItems array is directly manipulated in the
                            // dialog, so no further action is necessary
                        })
                .setPositiveButton(
                        R.string.dialog_ok,
                        (dialog, id) -> {
                            List<MessageItem> items = new ArrayList<>();
                            for (int i = 0; i < selectedItems.length; i++) {
                                if (selectedItems[i]) {
                                    items.add(MessageItem.settingValues()[i]);
                                }
                            }

                            String newValue = MessageConfiguration.toRawValue(newTopic, items);
                            callback.accept(newName, newValue);
                        })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create()
                .show();
    }

    private void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setTopic(@Nullable String topic) {
        this.topic = topic;
    }

    public void setSelectedItem(int index) {
        this.selectedItems[index] = true;
    }

    public interface Callback {
        void accept(@Nullable String newName, @Nullable String newValue);
    }
}
