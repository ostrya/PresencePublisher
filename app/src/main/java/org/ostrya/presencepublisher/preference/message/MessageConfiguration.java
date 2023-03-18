package org.ostrya.presencepublisher.preference.message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.ostrya.presencepublisher.mqtt.message.MessageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageConfiguration {
    @NonNull private final String topic;
    @NonNull private final List<MessageItem> items;

    @VisibleForTesting
    public MessageConfiguration(@NonNull String topic, @NonNull List<MessageItem> items) {
        this.topic = Objects.requireNonNull(topic);
        this.items = Objects.requireNonNull(items);
    }

    @Nullable
    public static MessageConfiguration fromRawValue(@Nullable String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String[] split = rawValue.split("\0");
        if (split.length < 2) {
            return null;
        }
        List<MessageItem> parsedItems = new ArrayList<>();
        for (int i = 1; i < split.length; i++) {
            MessageItem parsedItem = MessageItem.fromStringOrDefault(split[i], null);
            if (parsedItem == null) {
                return null;
            }
            parsedItems.add(parsedItem);
        }
        return new MessageConfiguration(split[0], parsedItems);
    }

    @NonNull
    public static String toRawValue(@NonNull String topic, @NonNull List<MessageItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append(topic);
        for (MessageItem item : items) {
            sb.append('\0').append(item.name());
        }
        return sb.toString();
    }

    @NonNull
    public String getTopic() {
        return topic;
    }

    @NonNull
    public List<MessageItem> getItems() {
        return items;
    }

    @VisibleForTesting
    public String getRawValue() {
        return toRawValue(topic, items);
    }
}
