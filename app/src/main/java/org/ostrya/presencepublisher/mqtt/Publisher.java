package org.ostrya.presencepublisher.mqtt;

import static org.ostrya.presencepublisher.message.Message.messagesForTopic;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_CONFIG_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_LIST;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageFormatPreference.MESSAGE_FORMAT_SETTING;
import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.VisibleForTesting;
import androidx.preference.PreferenceManager;

import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.message.Message;
import org.ostrya.presencepublisher.message.MessageContext;
import org.ostrya.presencepublisher.message.MessageFormat;
import org.ostrya.presencepublisher.message.MessageItem;
import org.ostrya.presencepublisher.network.NetworkService;
import org.ostrya.presencepublisher.schedule.Scheduler;
import org.ostrya.presencepublisher.ui.preference.messages.MessageConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Publisher {
    private static final String TAG = "Publisher";

    private final MessageContext messageContext;
    private final SharedPreferences sharedPreferences;
    private final MqttService mqttService;
    private final NetworkService networkService;
    private final Scheduler scheduler;

    public Publisher(Context context) {
        Context applicationContext = context.getApplicationContext();
        messageContext = new MessageContext(applicationContext);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        mqttService = new MqttService(applicationContext);
        networkService = new NetworkService(applicationContext, sharedPreferences);
        scheduler = new Scheduler(applicationContext);
    }

    @VisibleForTesting
    Publisher(
            MessageContext messageContext,
            SharedPreferences sharedPreferences,
            MqttService mqttService,
            NetworkService networkService,
            Scheduler scheduler) {
        this.messageContext = messageContext;
        this.sharedPreferences = sharedPreferences;
        this.mqttService = mqttService;
        this.networkService = networkService;
        this.scheduler = scheduler;
    }

    public void publish() {
        if (!networkService.sendMessageViaCurrentConnection()) {
            HyperLog.i(TAG, "Not connected to valid network, waiting for re-connect");
            scheduler.waitForNetworkReconnect();
            return;
        }
        try {
            List<Message> messages = getMessagesToSend();
            if (!messages.isEmpty()) {
                doSend(messages);
            }
        } catch (RuntimeException e) {
            HyperLog.w(TAG, "Error while getting messages to send", e);
        } finally {
            scheduler.scheduleNext();
        }
    }

    private void doSend(List<Message> messages) {
        HyperLog.d(TAG, "Sending messages");
        try {
            mqttService.sendMessages(messages);
            sharedPreferences.edit().putLong(LAST_SUCCESS, System.currentTimeMillis()).apply();
        } catch (Exception e) {
            HyperLog.w(TAG, "Error while sending messages", e);
        }
    }

    private List<Message> getMessagesToSend() {
        List<Message> messages = new ArrayList<>();
        MessageFormat messageFormat =
                MessageFormat.fromStringOrDefault(
                        sharedPreferences.getString(MESSAGE_FORMAT_SETTING, null),
                        MessageFormat.PLAINTEXT);
        for (String messageConfigName :
                sharedPreferences.getStringSet(MESSAGE_LIST, Collections.emptySet())) {
            MessageConfiguration messageConfiguration =
                    MessageConfiguration.fromRawValue(
                            sharedPreferences.getString(
                                    MESSAGE_CONFIG_PREFIX + messageConfigName, null));
            if (messageConfiguration == null) {
                HyperLog.w(
                        TAG,
                        "No valid message configuration for config '" + messageConfigName + "'");
            } else {
                Message.MessageBuilder builder = messagesForTopic(messageConfiguration.getTopic());
                for (MessageItem item : messageConfiguration.getItems()) {
                    item.apply(messageContext, builder);
                }
                messages.addAll(builder.build(messageFormat));
            }
        }
        return Collections.unmodifiableList(messages);
    }
}
