package org.ostrya.presencepublisher.message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hypertrack.hyperlog.HyperLog;

import org.ostrya.presencepublisher.R;

import java.util.Locale;

public enum MessageItem {
    CONDITION_CONTENT {
        @Override
        public void apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(messageContext.getConditionContentProvider().getConditionContents());
        }
    },
    BATTERY_LEVEL {
        @Override
        public void apply(MessageContext messageContext, Message.MessageBuilder builder) {
            builder.withEntry(messageContext.getBatteryLevelProvider().getBatteryLevel());
        }
    };

    private static final String TAG = "MessageItem";

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public abstract void apply(MessageContext messageContext, Message.MessageBuilder builder);

    /**
     * Note: the values returned here must match with the descriptions given in {@link
     * R.array.message_item_descriptions}
     */
    public static MessageItem[] settingValues() {
        return new MessageItem[] {CONDITION_CONTENT, BATTERY_LEVEL};
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
