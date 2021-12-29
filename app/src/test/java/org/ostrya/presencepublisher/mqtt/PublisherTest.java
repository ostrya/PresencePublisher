package org.ostrya.presencepublisher.mqtt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_CONFIG_PREFIX;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageCategorySupport.MESSAGE_LIST;
import static org.ostrya.presencepublisher.ui.preference.messages.MessageFormatPreference.MESSAGE_FORMAT_SETTING;

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
import org.ostrya.presencepublisher.message.BatteryLevelProvider;
import org.ostrya.presencepublisher.message.ConditionContentProvider;
import org.ostrya.presencepublisher.message.ListEntry;
import org.ostrya.presencepublisher.message.Message;
import org.ostrya.presencepublisher.message.MessageContext;
import org.ostrya.presencepublisher.message.MessageFormat;
import org.ostrya.presencepublisher.message.MessageItem;
import org.ostrya.presencepublisher.message.StringEntry;
import org.ostrya.presencepublisher.network.NetworkService;
import org.ostrya.presencepublisher.schedule.Scheduler;
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
    @Mock private BatteryLevelProvider batteryLevelProvider;
    @Mock private ConditionContentProvider conditionContentProvider;
    @Mock private MqttService mqttService;
    @Mock private NetworkService networkService;
    @Mock private Scheduler scheduler;
    @Mock private MessageContext messageContext;

    @Captor private ArgumentCaptor<List<Message>> messagesCaptor;

    private Publisher uut;

    @Before
    public void setup() {
        when(messageContext.getBatteryLevelProvider()).thenReturn(batteryLevelProvider);
        when(messageContext.getConditionContentProvider()).thenReturn(conditionContentProvider);
        when(sharedPreferences.getString(MESSAGE_FORMAT_SETTING, null)).thenReturn("PLAINTEXT");
        uut =
                new Publisher(
                        messageContext, sharedPreferences, mqttService, networkService, scheduler);
    }

    @Test
    public void publish_does_nothing_if_not_connected_to_valid_network() {
        when(networkService.sendMessageViaCurrentConnection()).thenReturn(false);
        String topic = "topic";
        setupMessages(
                new MessageConfiguration(
                        topic, Collections.singletonList(MessageItem.BATTERY_LEVEL)));
        StringEntry entry = new StringEntry("key", "99");
        lenient().when(batteryLevelProvider.getBatteryLevel()).thenReturn(entry);

        uut.publish();

        verifyNoInteractions(batteryLevelProvider, conditionContentProvider, mqttService);
        verify(scheduler).waitForNetworkReconnect();
    }

    @Test
    public void publish_does_nothing_if_no_messages_configured() {
        when(networkService.sendMessageViaCurrentConnection()).thenReturn(true);
        doReturn(Collections.emptySet())
                .when(sharedPreferences)
                .getStringSet(MESSAGE_LIST, Collections.emptySet());

        uut.publish();

        verifyNoInteractions(batteryLevelProvider, conditionContentProvider, mqttService);
        verify(scheduler).scheduleNext();
    }

    @Test
    public void publish_correctly_sends_message_with_battery_item() throws MqttException {
        when(networkService.sendMessageViaCurrentConnection()).thenReturn(true);
        String topic = "topic";
        setupMessages(
                new MessageConfiguration(
                        topic, Collections.singletonList(MessageItem.BATTERY_LEVEL)));
        StringEntry entry = new StringEntry("key", "99");
        when(batteryLevelProvider.getBatteryLevel()).thenReturn(entry);

        uut.publish();

        verifyNoInteractions(conditionContentProvider);
        verify(mqttService).sendMessages(messagesCaptor.capture());
        verify(scheduler).scheduleNext();

        List<Message> messages = messagesCaptor.getValue();
        assertThat(messages)
                .containsExactlyElementsOf(
                        Message.messagesForTopic(topic)
                                .withEntry(entry)
                                .build(MessageFormat.PLAINTEXT));
    }

    @Test
    public void publish_sends_no_message_with_condition_item_without_valid_condition_contents() {
        when(networkService.sendMessageViaCurrentConnection()).thenReturn(true);
        String topic = "topic";
        setupMessages(
                new MessageConfiguration(
                        topic, Collections.singletonList(MessageItem.CONDITION_CONTENT)));
        when(conditionContentProvider.getConditionContents())
                .thenReturn(new ListEntry("key", Collections.emptyList()));

        uut.publish();

        verifyNoInteractions(batteryLevelProvider, mqttService);
        verify(scheduler).scheduleNext();
    }

    @Test
    public void publish_correctly_sends_individual_messages_with_condition_items()
            throws MqttException {
        when(networkService.sendMessageViaCurrentConnection()).thenReturn(true);
        String topic = "topic";
        setupMessages(
                new MessageConfiguration(
                        topic, Collections.singletonList(MessageItem.CONDITION_CONTENT)));
        String content1 = "content1";
        String content2 = "content2";
        String content3 = "content3";
        ListEntry entry = new ListEntry("key", Arrays.asList(content1, content2, content3));
        when(conditionContentProvider.getConditionContents()).thenReturn(entry);

        uut.publish();

        verifyNoInteractions(batteryLevelProvider);
        verify(mqttService).sendMessages(messagesCaptor.capture());
        verify(scheduler).scheduleNext();

        List<Message> messages = messagesCaptor.getValue();
        assertThat(messages)
                .containsExactlyInAnyOrderElementsOf(
                        Message.messagesForTopic(topic)
                                .withEntry(entry)
                                .build(MessageFormat.PLAINTEXT));
    }

    @Test
    public void
            publish_correctly_sends_individual_messages_with_condition_placeholder_and_separate_message_with_battery_placeholder()
                    throws MqttException {
        doReturn(true).when(networkService).sendMessageViaCurrentConnection();
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
        ListEntry entry1 = new ListEntry("key1", Arrays.asList(content1, content2, content3));
        when(conditionContentProvider.getConditionContents()).thenReturn(entry1);
        StringEntry entry2 = new StringEntry("key", "99");
        when(batteryLevelProvider.getBatteryLevel()).thenReturn(entry2);

        uut.publish();

        verify(mqttService).sendMessages(messagesCaptor.capture());
        verify(scheduler).scheduleNext();

        List<Message> messages = messagesCaptor.getValue();

        List<Message> expected =
                Message.messagesForTopic(topic1).withEntry(entry1).build(MessageFormat.PLAINTEXT);
        expected.addAll(
                Message.messagesForTopic(topic2).withEntry(entry2).build(MessageFormat.PLAINTEXT));
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
