package org.ostrya.presencepublisher.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.hypertrack.hyperlog.HyperLog;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.mqtt.MqttService;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.ostrya.presencepublisher.message.Message.messageForTopic;

public class CheckConnectionDialogFragment extends DialogFragment {
    private static final String TAG = "CheckConnectionDialogFragment";

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Context context;
    private MqttService mqttService;

    public static CheckConnectionDialogFragment getInstance(final Context context) {
        CheckConnectionDialogFragment fragment = new CheckConnectionDialogFragment();
        fragment.setContext(context);
        fragment.setMqttService(new MqttService(context));
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.dialog_check_connection_title)
                .setMessage(R.string.dialog_check_connection_summary_waiting)
                .setNeutralButton(R.string.dialog_cancel, null);

        AlertDialog alertDialog = builder.create();
        Future<?> future = executorService.submit(new ConnectionTestWorker(alertDialog));
        alertDialog.setOnDismissListener(dialog -> future.cancel(true));
        return alertDialog;
    }

    private void setMqttService(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    private class ConnectionTestWorker implements Runnable {
        private final AlertDialog alertDialog;

        private ConnectionTestWorker(AlertDialog alertDialog) {
            this.alertDialog = alertDialog;
        }

        @Override
        public void run() {
            String message;
            try {
                mqttService.sendMessages(Collections.singletonList(messageForTopic("test").withContent("test")));
                message = getResources().getString(R.string.dialog_check_connection_summary_success);
            } catch (MqttException e) {
                HyperLog.w(TAG, "Error while sending message", e);
                message = String.format(context.getString(R.string.dialog_check_connection_summary_failure), e.getCause().getMessage());
            }
            final String result = message;
            requireActivity().runOnUiThread(() -> {
                alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setText(R.string.dialog_ok);
                alertDialog.setMessage(result);
            });
        }
    }
}
