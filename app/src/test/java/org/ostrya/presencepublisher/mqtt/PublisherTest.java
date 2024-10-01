package org.ostrya.presencepublisher.mqtt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.ostrya.presencepublisher.preference.message.MessageCategorySupport.MESSAGE_CONFIG_PREFIX;
import static org.ostrya.presencepublisher.preference.message.MessageCategorySupport.MESSAGE_LIST;
import static org.ostrya.presencepublisher.preference.message.MessageFormatPreference.MESSAGE_FORMAT_SETTING;
import static org.ostrya.presencepublisher.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ostrya.presencepublisher.mqtt.context.MessageContext;
import org.ostrya.presencepublisher.mqtt.context.MessageContextProvider;
import org.ostrya.presencepublisher.mqtt.context.device.BatteryStatus;
import org.ostrya.presencepublisher.mqtt.message.Message;
import org.ostrya.presencepublisher.mqtt.message.MessageFormat;
import org.ostrya.presencepublisher.mqtt.message.MessageItem;
import org.ostrya.presencepublisher.notification.NotificationFactory;
import org.ostrya.presencepublisher.preference.message.MessageConfiguration;
import org.ostrya.presencepublisher.test.LogDisablerRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@RunWith(Parameterized.class)
public class PublisherTest {
    private static final int BATTERY_VALUE = 99;
    private static final String CONDITION_CONTENT_1 = "content1";
    private static final String CONDITION_CONTENT_2 = "content2";
    private static final String CONDITION_CONTENT_3 = "content3";

    @Rule public final LogDisablerRule logDisablerRule = new LogDisablerRule();

    @Mock private SharedPreferences sharedPreferences;
    @Mock private SharedPreferences.Editor editor;
    @Mock private MessageContextProvider messageContextProvider;
    @Mock private MessageContext messageContext;
    @Mock private NotificationFactory notificationFactory;
    @Mock private MqttService mqttService;

    @Captor private ArgumentCaptor<List<Message>> messagesCaptor;

    private AutoCloseable mockContext;

    @Parameterized.Parameter public TestCase testCase;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<TestCase> data() {
        return Arrays.asList(
                new TestCase(MessageFormat.PLAINTEXT)
                        .withBatteryOnly("99")
                        .withContentOnly(
                                CONDITION_CONTENT_1, CONDITION_CONTENT_2, CONDITION_CONTENT_3)
                        .withBatteryAndContent(
                                "99",
                                CONDITION_CONTENT_1,
                                CONDITION_CONTENT_2,
                                CONDITION_CONTENT_3),
                new TestCase(MessageFormat.CSV)
                        .withBatteryOnly("\"batteryLevel=99\"")
                        .withContentOnly(
                                new StringJoiner(
                                                "\",\"conditionContent=",
                                                "\"conditionContent=",
                                                "\"")
                                        .add(CONDITION_CONTENT_1)
                                        .add(CONDITION_CONTENT_2)
                                        .add(CONDITION_CONTENT_3)
                                        .toString())
                        .withBatteryAndContent(
                                "\"batteryLevel=99\","
                                        + new StringJoiner(
                                                        "\",\"conditionContent=",
                                                        "\"conditionContent=",
                                                        "\"")
                                                .add(CONDITION_CONTENT_1)
                                                .add(CONDITION_CONTENT_2)
                                                .add(CONDITION_CONTENT_3)
                                                .toString()),
                new TestCase(MessageFormat.JSON)
                        .withBatteryOnly("{\n  \"batteryLevel\": 99\n}")
                        .withContentOnly(
                                "{\n  \"conditionContent\": "
                                        + new StringJoiner("\",\"", "[\"", "\"]")
                                                .add(CONDITION_CONTENT_1)
                                                .add(CONDITION_CONTENT_2)
                                                .add(CONDITION_CONTENT_3)
                                                .toString()
                                        + "\n}")
                        .withBatteryAndContent(
                                "{\n  \"batteryLevel\": 99,\n  \"conditionContent\": "
                                        + new StringJoiner("\",\"", "[\"", "\"]")
                                                .add(CONDITION_CONTENT_1)
                                                .add(CONDITION_CONTENT_2)
                                                .add(CONDITION_CONTENT_3)
                                                .toString()
                                        + "\n}"),
                new TestCase(MessageFormat.YAML)
                        .withBatteryOnly("batteryLevel: 99")
                        .withContentOnly(
                                "conditionContent: "
                                        + new StringJoiner(",", "[", "]")
                                                .add(CONDITION_CONTENT_1)
                                                .add(CONDITION_CONTENT_2)
                                                .add(CONDITION_CONTENT_3)
                                                .toString())
                        .withBatteryAndContent(
                                "batteryLevel: 99\nconditionContent: "
                                        + new StringJoiner(",", "[", "]")
                                                .add(CONDITION_CONTENT_1)
                                                .add(CONDITION_CONTENT_2)
                                                .add(CONDITION_CONTENT_3)
                                                .toString()));
    }

    private Publisher uut;

    private long currentTimestamp;
    private long nextTimestamp;

    @Before
    public void setup() {
        mockContext = MockitoAnnotations.openMocks(this);
        when(messageContextProvider.getContext()).thenReturn(messageContext);
        when(sharedPreferences.getString(MESSAGE_FORMAT_SETTING, null))
                .thenReturn(testCase.format.name());
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

    @After
    public void tearDown() throws Exception {
        mockContext.close();
    }

    @Test
    public void publish_sends_nothing_if_no_messages_configured() {
        when(sharedPreferences.getStringSet(MESSAGE_LIST, Collections.emptySet()))
                .thenReturn(Collections.emptySet());

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
        when(messageContext.getBatteryStatus())
                .thenReturn(new BatteryStatus("foo", BATTERY_VALUE, "bar"));

        uut.publish();

        verify(mqttService).sendMessages(messagesCaptor.capture());
        verify(notificationFactory).updateStatusNotification(currentTimestamp, nextTimestamp);
        verify(editor).putLong(NEXT_SCHEDULE, nextTimestamp);

        List<Message> messages = messagesCaptor.getValue();
        assertThat(messages)
                .containsExactlyElementsOf(
                        Message.messagesForTopic(topic).buildForTesting(testCase.batteryOnly));
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
    public void publish_correctly_sends_messages_with_condition_items() throws MqttException {
        String topic = "topic";
        setupMessages(
                new MessageConfiguration(
                        topic, Collections.singletonList(MessageItem.CONDITION_CONTENT)));
        List<String> values =
                Arrays.asList(CONDITION_CONTENT_1, CONDITION_CONTENT_2, CONDITION_CONTENT_3);
        when(messageContext.getConditionContents()).thenReturn(values);

        uut.publish();

        verify(mqttService).sendMessages(messagesCaptor.capture());
        verify(notificationFactory).updateStatusNotification(currentTimestamp, nextTimestamp);
        verify(editor).putLong(NEXT_SCHEDULE, nextTimestamp);

        List<Message> messages = messagesCaptor.getValue();
        assertThat(messages)
                .containsExactlyInAnyOrderElementsOf(
                        Message.messagesForTopic(topic).buildForTesting(testCase.contentOnly));
    }

    @Test
    public void
            publish_correctly_sends_message_with_condition_placeholder_and_separate_message_with_battery_placeholder()
                    throws MqttException {
        String topic1 = "topic1";
        String topic2 = "topic2";
        setupMessages(
                new MessageConfiguration(
                        topic1, Collections.singletonList(MessageItem.CONDITION_CONTENT)),
                new MessageConfiguration(
                        topic2, Collections.singletonList(MessageItem.BATTERY_LEVEL)));
        List<String> values =
                Arrays.asList(CONDITION_CONTENT_1, CONDITION_CONTENT_2, CONDITION_CONTENT_3);
        when(messageContext.getConditionContents()).thenReturn(values);
        when(messageContext.getBatteryStatus())
                .thenReturn(new BatteryStatus("foo", BATTERY_VALUE, "bar"));

        uut.publish();

        verify(mqttService).sendMessages(messagesCaptor.capture());
        verify(notificationFactory).updateStatusNotification(currentTimestamp, nextTimestamp);
        verify(editor).putLong(NEXT_SCHEDULE, nextTimestamp);

        List<Message> messages = messagesCaptor.getValue();

        List<Message> expected =
                Message.messagesForTopic(topic1).buildForTesting(testCase.contentOnly);
        expected.addAll(Message.messagesForTopic(topic2).buildForTesting(testCase.batteryOnly));
        assertThat(messages).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void
            publish_correctly_sends_messages_with_condition_items_and_combined_battery_placeholder()
                    throws MqttException {
        String topic = "topic";
        setupMessages(
                new MessageConfiguration(
                        topic,
                        Arrays.asList(MessageItem.CONDITION_CONTENT, MessageItem.BATTERY_LEVEL)));
        List<String> values =
                Arrays.asList(CONDITION_CONTENT_1, CONDITION_CONTENT_2, CONDITION_CONTENT_3);
        when(messageContext.getConditionContents()).thenReturn(values);
        when(messageContext.getBatteryStatus())
                .thenReturn(new BatteryStatus("foo", BATTERY_VALUE, "bar"));

        uut.publish();

        verify(mqttService).sendMessages(messagesCaptor.capture());
        verify(notificationFactory).updateStatusNotification(currentTimestamp, nextTimestamp);
        verify(editor).putLong(NEXT_SCHEDULE, nextTimestamp);

        List<Message> messages = messagesCaptor.getValue();
        assertThat(messages)
                .containsExactlyInAnyOrderElementsOf(
                        Message.messagesForTopic(topic)
                                .buildForTesting(testCase.batteryAndContent));
    }

    @Test
    public void
            publish_sends_no_messages_with_condition_items_and_combined_battery_placeholder_without_valid_condition_contents() {
        String topic = "topic";
        setupMessages(
                new MessageConfiguration(
                        topic,
                        Arrays.asList(MessageItem.CONDITION_CONTENT, MessageItem.BATTERY_LEVEL)));
        when(messageContext.getConditionContents()).thenReturn(Collections.emptyList());
        when(messageContext.getBatteryStatus())
                .thenReturn(new BatteryStatus("foo", BATTERY_VALUE, "bar"));

        uut.publish();

        verifyNoInteractions(mqttService, notificationFactory);
        verify(editor).putLong(NEXT_SCHEDULE, nextTimestamp);
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

    public static class TestCase {
        private final MessageFormat format;
        private final List<String> batteryOnly = new ArrayList<>();
        private final List<String> contentOnly = new ArrayList<>();
        private final List<String> batteryAndContent = new ArrayList<>();

        private TestCase(MessageFormat format) {
            this.format = format;
        }

        private TestCase withBatteryOnly(String... batteryOnly) {
            this.batteryOnly.addAll(Arrays.asList(batteryOnly));
            return this;
        }

        private TestCase withContentOnly(String... contentOnly) {
            this.contentOnly.addAll(Arrays.asList(contentOnly));
            return this;
        }

        private TestCase withBatteryAndContent(String... batteryAndContent) {
            this.batteryAndContent.addAll(Arrays.asList(batteryAndContent));
            return this;
        }

        @NonNull
        @Override
        public String toString() {
            return format.toString();
        }
    }
}
