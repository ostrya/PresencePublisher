package org.ostrya.presencepublisher.mqtt.message;

import static org.ostrya.presencepublisher.mqtt.context.MessageContext.UNKNOWN;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.context.MessageContext;

public enum MessageItem {
    CONDITION_CONTENT {
        @Override
        public boolean apply(MessageContext messageContext, Message.MessageBuilder builder) {
            if (messageContext.getConditionContents().isEmpty()) {
                return false;
            }
            builder.withEntry(this, messageContext.getConditionContents());
            return true;
        }
    },
    BATTERY_LEVEL {
        @Override
        public boolean apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(this, messageContext.getBatteryStatus().getBatteryLevelPercentage());
            return true;
        }
    },
    CHARGING_STATE {
        @Override
        public boolean apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(this, messageContext.getBatteryStatus().getBatteryStatus());
            return true;
        }
    },
    PLUG_STATE {
        @Override
        public boolean apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(this, messageContext.getBatteryStatus().getPlugStatus());
            return true;
        }
    },
    CONNECTED_WIFI {
        @Override
        public boolean apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(
                    this,
                    messageContext.getCurrentSsid() == null
                            ? UNKNOWN
                            : messageContext.getCurrentSsid());
            return true;
        }
    },
    CONNECTED_WIFI_BSSID {
        @Override
        public boolean apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(
                    this,
                    messageContext.getCurrentBssid() == null
                            ? UNKNOWN
                            : messageContext.getCurrentBssid());
            return true;
        }
    },
    GEO_LOCATION {
        @Override
        public boolean apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(this, messageContext.getLastKnownLocation());
            return true;
        }
    },
    CURRENT_TIMESTAMP {
        @Override
        public boolean apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(this, messageContext.getCurrentTimestamp() / 1000);
            return true;
        }
    },
    NEXT_SCHEDULED_TIMESTAMP {
        @Override
        public boolean apply(MessageContext messageContext, Message.MessageBuilder builder) {
            long nextTimestamp = messageContext.getEstimatedNextTimestamp();
            builder.withEntry(this, nextTimestamp > 0L ? nextTimestamp / 1000 : nextTimestamp);
            return true;
        }
    },
    NEXT_ALARMCLOCK_TIMESTAMP {
        @Override
        public boolean apply(MessageContext messageContext, Message.MessageBuilder builder) {
            long nextAlarmclockTimestamp = messageContext.getNextAlarmclockTimestamp();
            builder.withEntry(
                    this,
                    nextAlarmclockTimestamp > 0L
                            ? nextAlarmclockTimestamp / 1000
                            : nextAlarmclockTimestamp);
            return true;
        }
    },
    DEVICE_NAME {
        @Override
        public boolean apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(this, messageContext.getDeviceName());
            return true;
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
     * #settingDescriptions()}
     */
    public static MessageItem[] settingValues() {
        return new MessageItem[] {
            CONDITION_CONTENT,
            BATTERY_LEVEL,
            CHARGING_STATE,
            PLUG_STATE,
            CONNECTED_WIFI,
            CONNECTED_WIFI_BSSID,
            GEO_LOCATION,
            CURRENT_TIMESTAMP,
            NEXT_SCHEDULED_TIMESTAMP,
            NEXT_ALARMCLOCK_TIMESTAMP,
            DEVICE_NAME
        };
    }

    public abstract boolean apply(MessageContext messageContext, Message.MessageBuilder builder);

    public String getName() {
        return name;
    }

    public static int settingDescriptions() {
        return R.array.message_item_descriptions;
    }

    public static MessageItem fromStringOrDefault(
            @NonNull String rawValue, @Nullable MessageItem defaultValue) {
        try {
            return MessageItem.valueOf(rawValue);
        } catch (IllegalArgumentException e) {
            DatabaseLogger.w(
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
