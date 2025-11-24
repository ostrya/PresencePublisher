package org.ostrya.presencepublisher.mqtt.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.ostrya.presencepublisher.mqtt.message.MessageItem.BATTERY_LEVEL;
import static org.ostrya.presencepublisher.mqtt.message.MessageItem.CHARGING_STATE;
import static org.ostrya.presencepublisher.mqtt.message.MessageItem.CONDITION_CONTENT;
import static org.ostrya.presencepublisher.mqtt.message.MessageItem.CONNECTED_WIFI;
import static org.ostrya.presencepublisher.mqtt.message.MessageItem.CURRENT_TIMESTAMP;
import static org.ostrya.presencepublisher.mqtt.message.MessageItem.DEVICE_NAME;
import static org.ostrya.presencepublisher.mqtt.message.MessageItem.GEO_LOCATION;
import static org.ostrya.presencepublisher.mqtt.message.MessageItem.NEXT_ALARMCLOCK_TIMESTAMP;
import static org.ostrya.presencepublisher.mqtt.message.MessageItem.NEXT_SCHEDULED_TIMESTAMP;
import static org.ostrya.presencepublisher.mqtt.message.MessageItem.PLUG_STATE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ostrya.presencepublisher.mqtt.context.MessageContext;
import org.ostrya.presencepublisher.mqtt.context.device.BatteryStatus;

import java.util.Arrays;
import java.util.HashSet;

@RunWith(MockitoJUnitRunner.class)
public class MessageItemTest {
    @Mock MessageContext context;

    // as the message format has its own testing, we're only using YAML here for simplicity

    @Test
    public void condition_content_works() {
        Message.MessageBuilder builder = Message.messagesForTopic("topic");

        when(context.getConditionContents()).thenReturn(Arrays.asList("foo", "bar"));

        CONDITION_CONTENT.apply(context, builder);

        assertThat(builder.build(MessageFormat.YAML))
                .hasSize(1)
                .element(0)
                .extracting(Message::getContent)
                .isEqualTo("conditionContent: [foo,bar]");
    }

    @Test
    public void battery_level_works() {
        Message.MessageBuilder builder = Message.messagesForTopic("topic");

        when(context.getBatteryStatus()).thenReturn(new BatteryStatus("kaput", 123, "plugin"));

        BATTERY_LEVEL.apply(context, builder);

        assertThat(builder.build(MessageFormat.YAML))
                .hasSize(1)
                .element(0)
                .extracting(Message::getContent)
                .isEqualTo("batteryLevel: 123");
    }

    @Test
    public void charging_state_works() {
        Message.MessageBuilder builder = Message.messagesForTopic("topic");

        when(context.getBatteryStatus()).thenReturn(new BatteryStatus("kaput", 123, "plugin"));

        CHARGING_STATE.apply(context, builder);

        assertThat(builder.build(MessageFormat.YAML))
                .hasSize(1)
                .element(0)
                .extracting(Message::getContent)
                .isEqualTo("chargingState: kaput");
    }

    @Test
    public void plug_state_works() {
        Message.MessageBuilder builder = Message.messagesForTopic("topic");

        when(context.getBatteryStatus()).thenReturn(new BatteryStatus("kaput", 123, "plugin"));

        PLUG_STATE.apply(context, builder);

        assertThat(builder.build(MessageFormat.YAML))
                .hasSize(1)
                .element(0)
                .extracting(Message::getContent)
                .isEqualTo("plugState: plugin");
    }

    @Test
    public void connected_wifi_works() {
        Message.MessageBuilder builder = Message.messagesForTopic("topic");

        when(context.getCurrentSsids()).thenReturn(new HashSet<>(Arrays.asList("SSID1", "SSID2")));

        CONNECTED_WIFI.apply(context, builder);

        assertThat(builder.build(MessageFormat.YAML))
                .hasSize(1)
                .element(0)
                .extracting(Message::getContent)
                .isEqualTo("connectedWifi: [SSID1,SSID2]");
    }

    @Test
    public void disconnected_wifi_works() {
        Message.MessageBuilder builder = Message.messagesForTopic("topic");

        when(context.getCurrentSsids()).thenReturn(new HashSet<>());

        CONNECTED_WIFI.apply(context, builder);

        assertThat(builder.build(MessageFormat.YAML))
                .hasSize(1)
                .element(0)
                .extracting(Message::getContent)
                .isEqualTo("connectedWifi: []");
    }

    @Test
    public void geo_location_works() {
        Message.MessageBuilder builder = Message.messagesForTopic("topic");

        when(context.getLastKnownLocation()).thenReturn("geo:123,45;u=67;timestamp=987654321");

        GEO_LOCATION.apply(context, builder);

        assertThat(builder.build(MessageFormat.YAML))
                .hasSize(1)
                .element(0)
                .extracting(Message::getContent)
                .isEqualTo("geoLocation: geo:123,45;u=67;timestamp=987654321");
    }

    @Test
    public void current_timestamp_works() {
        Message.MessageBuilder builder = Message.messagesForTopic("topic");

        when(context.getCurrentTimestamp()).thenReturn(987654321L);

        CURRENT_TIMESTAMP.apply(context, builder);

        assertThat(builder.build(MessageFormat.YAML))
                .hasSize(1)
                .element(0)
                .extracting(Message::getContent)
                .isEqualTo("currentTimestamp: 987654");
    }

    @Test
    public void next_scheduled_timestamp_works() {
        Message.MessageBuilder builder = Message.messagesForTopic("topic");

        when(context.getEstimatedNextTimestamp()).thenReturn(123456789L);

        NEXT_SCHEDULED_TIMESTAMP.apply(context, builder);

        assertThat(builder.build(MessageFormat.YAML))
                .hasSize(1)
                .element(0)
                .extracting(Message::getContent)
                .isEqualTo("nextScheduledTimestamp: 123456");
    }

    @Test
    public void next_alarmclock_timestamp_works() {
        Message.MessageBuilder builder = Message.messagesForTopic("topic");

        when(context.getNextAlarmclockTimestamp()).thenReturn(111222333L);

        NEXT_ALARMCLOCK_TIMESTAMP.apply(context, builder);

        assertThat(builder.build(MessageFormat.YAML))
                .hasSize(1)
                .element(0)
                .extracting(Message::getContent)
                .isEqualTo("nextAlarmclockTimestamp: 111222");
    }

    @Test
    public void device_name_works() {
        Message.MessageBuilder builder = Message.messagesForTopic("topic");

        when(context.getDeviceName()).thenReturn("my Iphone");

        DEVICE_NAME.apply(context, builder);

        assertThat(builder.build(MessageFormat.YAML))
                .hasSize(1)
                .element(0)
                .extracting(Message::getContent)
                .isEqualTo("deviceName: my Iphone");
    }

    @Test
    public void all_items_are_tested() {
        assertThat(MessageItem.values())
                .as(
                        "If this assertion fails, please fix it and add an according test case to"
                                + " the new item(s)")
                .hasSize(10);
    }
}
