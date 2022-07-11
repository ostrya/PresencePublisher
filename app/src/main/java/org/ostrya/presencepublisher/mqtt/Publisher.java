package org.ostrya.presencepublisher.mqtt;

import static org.ostrya.presencepublisher.message.Message.messagesForTopic;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_CONFIG_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_LIST;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageFormatPreference.MESSAGE_FORMAT_SETTING;
import static org.ostrya.presencepublisher.ui.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.VisibleForTesting;
import androidx.core.util.Supplier;
import androidx.preference.PreferenceManager;

import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.message.AlarmclockTimestampProvider;
import org.ostrya.presencepublisher.message.BatteryStatusProvider;
import org.ostrya.presencepublisher.message.ConditionContentProvider;
import org.ostrya.presencepublisher.message.LocationProvider;
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

    private final AlarmclockTimestampProvider alarmclockTimestampProvider;
    private final Supplier<BatteryStatusProvider> batteryStatusProviderSupplier;
    private final ConditionContentProvider conditionContentProvider;
    private final LocationProvider locationProvider;
    private final SharedPreferences sharedPreferences;
    private final MqttService mqttService;
    private final NetworkService networkService;
    private final Scheduler scheduler;

    public Publisher(Context context) {
        Context applicationContext = context.getApplicationContext();
        alarmclockTimestampProvider = new AlarmclockTimestampProvider(applicationContext);
        batteryStatusProviderSupplier = () -> new BatteryStatusProvider(applicationContext);
        conditionContentProvider = new ConditionContentProvider(applicationContext);
        locationProvider = new LocationProvider(applicationContext);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        mqttService = new MqttService(applicationContext);
        networkService = new NetworkService(applicationContext, sharedPreferences);
        scheduler = new Scheduler(applicationContext);
    }

    @VisibleForTesting
    Publisher(
            AlarmclockTimestampProvider alarmclockTimestampProvider,
            BatteryStatusProvider batteryStatusProvider,
            ConditionContentProvider conditionContentProvider,
            LocationProvider locationProvider,
            SharedPreferences sharedPreferences,
            MqttService mqttService,
            NetworkService networkService,
            Scheduler scheduler) {
        this.alarmclockTimestampProvider = alarmclockTimestampProvider;
        this.batteryStatusProviderSupplier = () -> batteryStatusProvider;
        this.locationProvider = locationProvider;
        this.conditionContentProvider = conditionContentProvider;
        this.sharedPreferences = sharedPreferences;
        this.mqttService = mqttService;
        this.networkService = networkService;
        this.scheduler = scheduler;
    }

    public void publish() {
        BatteryStatusProvider batteryStatusProvider = batteryStatusProviderSupplier.get();
        if (!networkService.sendMessageViaCurrentConnection()) {
            DatabaseLogger.i(TAG, "Not connected to valid network, waiting for re-connect");
            scheduler.waitForNetworkReconnect(batteryStatusProvider.isCharging());
            return;
        }

        try {
            long next = scheduler.scheduleNext(batteryStatusProvider.isCharging());
            MessageContext messageContext =
                    new MessageContext(
                            alarmclockTimestampProvider,
                            batteryStatusProvider,
                            conditionContentProvider,
                            locationProvider,
                            networkService.getCurrentSsid(),
                            next);
            List<Message> messages = getMessagesToSend(messageContext);
            if (!messages.isEmpty()) {
                doSend(messages, messageContext.getCurrentTimestamp());
            }
        } catch (RuntimeException e) {
            DatabaseLogger.w(TAG, "Error while getting messages to send", e);
        }
    }

    private void doSend(List<Message> messages, long currentTimestamp) {
        DatabaseLogger.d(TAG, "Sending messages");
        try {
            mqttService.sendMessages(messages);
            sharedPreferences.edit().putLong(LAST_SUCCESS, currentTimestamp).apply();
        } catch (Exception e) {
            DatabaseLogger.w(TAG, "Error while sending messages", e);
        }
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
