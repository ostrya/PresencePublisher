package org.ostrya.presencepublisher.mqtt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_CONFIG_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_LIST;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageFormatPreference.MESSAGE_FORMAT_SETTING;
import static org.ostrya.presencepublisher.ui.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;

import android.content.SharedPreferences;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ostrya.presencepublisher.message.BatteryStatus;
import org.ostrya.presencepublisher.message.Message;
import org.ostrya.presencepublisher.message.MessageContext;
import org.ostrya.presencepublisher.message.MessageContextProvider;
import org.ostrya.presencepublisher.message.MessageFormat;
import org.ostrya.presencepublisher.message.MessageItem;
import org.ostrya.presencepublisher.notification.NotificationFactory;
import org.ostrya.presencepublisher.test.LogDisablerRule;
import org.ostrya.presencepublisher.ui.preference.messages.MessageConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class PublisherTest {
    @Rule public final LogDisablerRule logDisablerRule = new LogDisablerRule();

    @Mock private SharedPreferences sharedPreferences;
    @Mock private SharedPreferences.Editor editor;
    @Mock private MessageContextProvider messageContextProvider;
    @Mock private MessageContext messageContext;
    @Mock private NotificationFactory notificationFactory;
    @Mock private MqttService mqttService;

    @Captor private ArgumentCaptor<List<Message>> messagesCaptor;

    private Publisher uut;

    private long currentTimestamp;
    private long nextTimestamp;

    @Before
    public void setup() {
        when(messageContextProvider.getContext()).thenReturn(messageContext);
        when(sharedPreferences.getString(MESSAGE_FORMAT_SETTING, null)).thenReturn("PLAINTEXT");
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        currentTimestamp = (long) (Math.random() * 100) + 1;
        nextTimestamp = currentTimestamp + 1;
        when(messageContext.getCurrentTimestamp()).thenReturn(currentTimestamp);
        when(messageContext.getEstimatedNextTimestamp()).thenReturn(nextTimestamp);
        uut =
                new Publisher(
                        messageContextProvider,
                        notificationFactory,
                        sharedPreferences,
                        mqttService);
    }

    @Test
    public void publish_sends_nothing_if_no_messages_configured() {
        doReturn(Collections.emptySet())
                .when(sharedPreferences)
                .getStringSet(MESSAGE_LIST, Collections.emptySet());

        uut.publish();

        verifyNoInteractions(mqttService, notificationFactory);
        verify(editor).putLong(NEXT_SCHEDULE, nextTimestamp);
    }

    @Test
    public void publish_correctly_sends_message_with_battery_item() throws MqttException {
        String topic = "topic";
        setupMessages(
                new MessageConfiguration(
                        topic, Collections.singletonList(MessageItem.BATTERY_LEVEL)));
        int value = 99;
        when(messageContext.getBatteryStatus()).thenReturn(new BatteryStatus("foo", value, "bar"));

        uut.publish();

        verify(mqttService).sendMessages(messagesCaptor.capture());
        verify(notificationFactory).updateStatusNotification(currentTimestamp, nextTimestamp);
        verify(editor).putLong(NEXT_SCHEDULE, nextTimestamp);

        List<Message> messages = messagesCaptor.getValue();
        assertThat(messages)
                .containsExactlyElementsOf(
                        Message.messagesForTopic(topic)
                                .withEntry(MessageItem.BATTERY_LEVEL, value)
                                .build(MessageFormat.PLAINTEXT));
    }

    @Test
    public void publish_sends_no_message_with_condition_item_without_valid_condition_contents() {
        String topic = "topic";
        setupMessages(
                new MessageConfiguration(
                        topic, Collections.singletonList(MessageItem.CONDITION_CONTENT)));
        when(messageContext.getConditionContents()).thenReturn(Collections.emptyList());

        uut.publish();

        verifyNoInteractions(mqttService, notificationFactory);
        verify(editor).putLong(NEXT_SCHEDULE, nextTimestamp);
    }

    @Test
    public void publish_correctly_sends_individual_messages_with_condition_items()
            throws MqttException {
        String topic = "topic";
        setupMessages(
                new MessageConfiguration(
                        topic, Collections.singletonList(MessageItem.CONDITION_CONTENT)));
        String content1 = "content1";
        String content2 = "content2";
        String content3 = "content3";
        List<String> values = Arrays.asList(content1, content2, content3);
        when(messageContext.getConditionContents()).thenReturn(values);

        uut.publish();

        verify(mqttService).sendMessages(messagesCaptor.capture());
        verify(notificationFactory).updateStatusNotification(currentTimestamp, nextTimestamp);
        verify(editor).putLong(NEXT_SCHEDULE, nextTimestamp);

        List<Message> messages = messagesCaptor.getValue();
        assertThat(messages)
                .containsExactlyInAnyOrderElementsOf(
                        Message.messagesForTopic(topic)
                                .withEntry(MessageItem.CONDITION_CONTENT, values)
                                .build(MessageFormat.PLAINTEXT));
    }

    @Test
    public void
            publish_correctly_sends_individual_messages_with_condition_placeholder_and_separate_message_with_battery_placeholder()
                    throws MqttException {
        String topic1 = "topic1";
        String topic2 = "topic2";
        setupMessages(
                new MessageConfiguration(
                        topic1, Collections.singletonList(MessageItem.CONDITION_CONTENT)),
                new MessageConfiguration(
                        topic2, Collections.singletonList(MessageItem.BATTERY_LEVEL)));
        String content1 = "content1";
        String content2 = "content2";
        String content3 = "content3";
        List<String> values = Arrays.asList(content1, content2, content3);
        when(messageContext.getConditionContents()).thenReturn(values);
        int value = 99;
        when(messageContext.getBatteryStatus()).thenReturn(new BatteryStatus("foo", value, "bar"));

        uut.publish();

        verify(mqttService).sendMessages(messagesCaptor.capture());
        verify(notificationFactory).updateStatusNotification(currentTimestamp, nextTimestamp);
        verify(editor).putLong(NEXT_SCHEDULE, nextTimestamp);

        List<Message> messages = messagesCaptor.getValue();

        List<Message> expected =
                Message.messagesForTopic(topic1)
                        .withEntry(MessageItem.CONDITION_CONTENT, values)
                        .build(MessageFormat.PLAINTEXT);
        expected.addAll(
                Message.messagesForTopic(topic2)
                        .withEntry(MessageItem.BATTERY_LEVEL, value)
                        .build(MessageFormat.PLAINTEXT));
        assertThat(messages).containsExactlyInAnyOrderElementsOf(expected);
    }

    private void setupMessages(MessageConfiguration... messageConfigurations) {
        Set<String> names = new HashSet<>();
        for (int i = 0; i < messageConfigurations.length; i++) {
            MessageConfiguration configuration = messageConfigurations[i];
            String generatedName = Integer.toString(i);
            names.add(generatedName);
            doReturn(configuration.getRawValue())
                    .when(sharedPreferences)
                    .getString(MESSAGE_CONFIG_PREFIX + generatedName, null);
        }
        doReturn(names).when(sharedPreferences).getStringSet(MESSAGE_LIST, Collections.emptySet());
    }
}
