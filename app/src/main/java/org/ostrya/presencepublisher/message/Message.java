package org.ostrya.presencepublisher.message;

public class Message {
    private final String topic;
    private final String content;

    private Message(String topic, String content) {
        this.content = content;
        this.topic = topic;
    }

    public static MessageBuilder messageForTopic(String topic) {
        return new MessageBuilder(topic);
    }

    public String getContent() {
        return content;
    }

    public String getTopic() {
        return topic;
    }

    public static final class MessageBuilder {
        private final String topic;

        private MessageBuilder(String topic) {
            this.topic = topic;
        }

        public Message withContent(String content) {
            return new Message(topic, content);
        }
    }
}
