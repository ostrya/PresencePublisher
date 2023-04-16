package org.ostrya.presencepublisher.preference.message;

import android.content.Context;

import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.dialog.ScrollableMessageFragment;
import org.ostrya.presencepublisher.mqtt.message.ListEntry;
import org.ostrya.presencepublisher.mqtt.message.MessageFormat;
import org.ostrya.presencepublisher.mqtt.message.MessageItem;
import org.ostrya.presencepublisher.mqtt.message.NumberEntry;
import org.ostrya.presencepublisher.mqtt.message.StringEntry;
import org.ostrya.presencepublisher.preference.common.ClickDummy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessageFormatHelpDummy extends ClickDummy {
    public MessageFormatHelpDummy(Context context, Fragment fragment) {
        super(
                context,
                R.drawable.baseline_help_24,
                R.string.message_format_help_title,
                R.string.message_format_help_summary,
                fragment);
    }

    @Override
    protected void onClick() {
        StringBuilder sb = new StringBuilder();
        for (MessageFormat format : MessageFormat.values()) {
            sb.append(messagePerFormat(format));
        }
        String content = sb.toString().replace("\n", "<br/>").replace(' ', ' ');
        CharSequence message = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);
        ScrollableMessageFragment fragment = ScrollableMessageFragment.getInstance(message);

        fragment.show(getParentFragmentManager(), null);
    }

    private String messagePerFormat(MessageFormat messageFormat) {
        StringBuilder sb = new StringBuilder();
        List<String> messages =
                messageFormat.formatContent(
                        Collections.singletonList(
                                new StringEntry(MessageItem.CONNECTED_WIFI.getName(), "myWiFi")),
                        Collections.singletonList(
                                new NumberEntry(MessageItem.BATTERY_LEVEL.getName(), 35)),
                        Collections.singletonList(
                                new ListEntry(
                                        MessageItem.CONDITION_CONTENT.getName(),
                                        Arrays.asList("contentA", "contentB"))));
        sb.append("<b>").append(messageFormat.name()).append("</b>").append("\n");
        for (String message : messages) {
            sb.append("<blockquote>").append(message).append("</blockquote>");
        }
        return sb.toString();
    }
}
