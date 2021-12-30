package org.ostrya.presencepublisher.message;

import static org.ostrya.presencepublisher.message.MessageContext.UNKNOWN;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.R;

public enum MessageItem {
    CONDITION_CONTENT {
        @Override
        public void apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(
                    this,
                    messageContext
                            .getConditionContentProvider()
                            .getConditionContents(messageContext.getCurrentSsid()));
        }
    },
    BATTERY_LEVEL {
        @Override
        public void apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(
                    this, messageContext.getBatteryStatusProvider().getBatteryLevelPercentage());
        }
    },
    CHARGING_STATE {
        @Override
        public void apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(this, messageContext.getBatteryStatusProvider().getBatteryStatus());
        }
    },
    PLUG_STATE {
        @Override
        public void apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(this, messageContext.getBatteryStatusProvider().getPlugStatus());
        }
    },
    CONNECTED_WIFI {
        @Override
        public void apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(
                    this,
                    messageContext.getCurrentSsid() == null
                            ? UNKNOWN
                            : messageContext.getCurrentSsid());
        }
    },
    GEO_LOCATION {
        @Override
        public void apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(this, messageContext.getLocationProvider().getLastKnownLocation());
        }
    },
    CURRENT_TIMESTAMP {
        @Override
        public void apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(this, messageContext.getCurrentTimestamp() / 1000);
        }
    },
    NEXT_SCHEDULED_TIMESTAMP {
        @Override
        public void apply(MessageContext messageContext, Message.MessageBuilder builder) {
            long nextTimestamp = messageContext.getNextTimestamp();
            builder.withEntry(this, nextTimestamp > 0L ? nextTimestamp / 1000 : nextTimestamp);
        }
    };

    private static final String TAG = "MessageItem";

    private final String name;

    MessageItem() {
        name = toCamelCase(name());
    }

    @VisibleForTesting
    static String toCamelCase(String original) {
        StringBuilder sb = new StringBuilder(original);
        boolean capitalize = false;
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '_') {
                sb.deleteCharAt(i);
                capitalize = true;
                i--;
            } else if (capitalize) {
                sb.setCharAt(i, Character.toTitleCase(sb.charAt(i)));
                capitalize = false;
            } else {
                sb.setCharAt(i, Character.toLowerCase(sb.charAt(i)));
            }
        }
        return sb.toString();
    }

    /**
     * Note: the values returned here must match with the descriptions given in {@link
     * #settingsDescriptions()}
     */
    public static MessageItem[] settingValues() {
        return new MessageItem[] {
            CONDITION_CONTENT,
            BATTERY_LEVEL,
            CHARGING_STATE,
            PLUG_STATE,
            CONNECTED_WIFI,
            GEO_LOCATION,
            CURRENT_TIMESTAMP,
            NEXT_SCHEDULED_TIMESTAMP
        };
    }

    public abstract void apply(MessageContext messageContext, Message.MessageBuilder builder);

    public String getName() {
        return name;
    }

    public static int settingsDescriptions() {
        return R.array.message_item_descriptions;
    }

    public static MessageItem fromStringOrDefault(
            @NonNull String rawValue, @Nullable MessageItem defaultValue) {
        try {
            return MessageItem.valueOf(rawValue);
        } catch (IllegalArgumentException e) {
            HyperLog.w(
                    TAG,
                    "Invalid setting '"
                            + rawValue
                            + "'. Used default value '"
                            + defaultValue
                            + "'.");
            return defaultValue;
        }
    }
}
