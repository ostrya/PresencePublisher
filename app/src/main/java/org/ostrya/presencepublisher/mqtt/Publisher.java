package org.ostrya.presencepublisher.mqtt;

import static org.ostrya.presencepublisher.mqtt.message.Message.messagesForTopic;
import static org.ostrya.presencepublisher.preference.message.MessageCategorySupport.MESSAGE_CONFIG_PREFIX;
import static org.ostrya.presencepublisher.preference.message.MessageCategorySupport.MESSAGE_LIST;
import static org.ostrya.presencepublisher.preference.message.MessageFormatPreference.MESSAGE_FORMAT_SETTING;
import static org.ostrya.presencepublisher.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.VisibleForTesting;
import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.context.MessageContext;
import org.ostrya.presencepublisher.mqtt.context.MessageContextProvider;
import org.ostrya.presencepublisher.mqtt.message.Message;
import org.ostrya.presencepublisher.mqtt.message.MessageFormat;
import org.ostrya.presencepublisher.mqtt.message.MessageItem;
import org.ostrya.presencepublisher.notification.NotificationFactory;
import org.ostrya.presencepublisher.preference.message.MessageConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Publisher {
    private static final String TAG = "Publisher";

    private final MessageContextProvider messageContextProvider;
    private final NotificationFactory notificationFactory;
    private final SharedPreferences sharedPreferences;
    private final MqttService mqttService;

    public Publisher(Context context) {
        Context applicationContext = context.getApplicationContext();
        messageContextProvider = new MessageContextProvider(applicationContext);
        notificationFactory = new NotificationFactory(applicationContext);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        mqttService = new MqttService(applicationContext);
    }

    @VisibleForTesting
    Publisher(
            MessageContextProvider messageContextProvider,
            NotificationFactory notificationFactory,
            SharedPreferences sharedPreferences,
            MqttService mqttService) {
        this.messageContextProvider = messageContextProvider;
        this.notificationFactory = notificationFactory;
        this.sharedPreferences = sharedPreferences;
        this.mqttService = mqttService;
    }

    public boolean publish() {
        MessageContext messageContext = messageContextProvider.getContext();
        long currentTimestamp = messageContext.getCurrentTimestamp();
        long estimatedNextTimestamp = messageContext.getEstimatedNextTimestamp();
        sharedPreferences.edit().putLong(NEXT_SCHEDULE, estimatedNextTimestamp).apply();
        List<Message> messages = getMessagesToSend(messageContext);
        if (!messages.isEmpty()) {
            DatabaseLogger.d(TAG, "Sending messages");
            try {
                mqttService.sendMessages(messages);
            } catch (Exception e) {
                DatabaseLogger.w(TAG, "Error while sending messages: " + messages, e);
                DatabaseLogger.logMessageError("Failed to send messages: " + e.getMessage());
                return false;
            }
            sharedPreferences.edit().putLong(LAST_SUCCESS, currentTimestamp).apply();
            notificationFactory.updateStatusNotification(currentTimestamp, estimatedNextTimestamp);
        }
        return true;
    }

    private List<Message> getMessagesToSend(MessageContext messageContext) {
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
                DatabaseLogger.w(
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
