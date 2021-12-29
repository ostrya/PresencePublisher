package org.ostrya.presencepublisher.message;

import androidx.annotation.NonNull;

import org.ostrya.presencepublisher.util.ObjectUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Message {
    @NonNull private final String topic;
    @NonNull private final String content;

    private Message(@NonNull String topic, @NonNull String content) {
        this.content = Objects.requireNonNull(content);
        this.topic = Objects.requireNonNull(topic);
    }

    public static MessageBuilder messagesForTopic(@NonNull String topic) {
        return new MessageBuilder(Objects.requireNonNull(topic));
    }

    @NonNull
    public String getContent() {
        return content;
    }

    @NonNull
    public String getTopic() {
        return topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return topic.equals(message.topic) && content.equals(message.content);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hash(topic, content);
    }

    @Override
    public String toString() {
        return "Message{" + "topic='" + topic + '\'' + ", content='" + content + '\'' + '}';
    }

    public static final class MessageBuilder {
        private final String topic;
        private final List<StringEntry> stringEntries = new ArrayList<>();
        private final List<ListEntry> listEntries = new ArrayList<>();

        private MessageBuilder(@NonNull String topic) {
            this.topic = Objects.requireNonNull(topic);
        }

        public MessageBuilder withEntry(@NonNull StringEntry entry) {
            stringEntries.add(Objects.requireNonNull(entry));
            return this;
        }

        public MessageBuilder withEntry(@NonNull ListEntry entry) {
            listEntries.add(Objects.requireNonNull(entry));
            return this;
        }

        public List<Message> build(@NonNull MessageFormat format) {
            List<Message> messages = new ArrayList<>();
            for (String content : format.formatContent(stringEntries, listEntries)) {
                messages.add(new Message(topic, content));
            }
            return messages;
        }
    }
}
