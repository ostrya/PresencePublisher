package org.ostrya.presencepublisher.ui.preference.messages;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.Html;

import androidx.fragment.app.Fragment;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.message.ListEntry;
import org.ostrya.presencepublisher.message.MessageFormat;
import org.ostrya.presencepublisher.message.MessageItem;
import org.ostrya.presencepublisher.message.NumberEntry;
import org.ostrya.presencepublisher.message.StringEntry;
import org.ostrya.presencepublisher.ui.dialog.ScrollableMessageFragment;
import org.ostrya.presencepublisher.ui.preference.common.ClickDummy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessageFormatHelpDummy extends ClickDummy {
    public MessageFormatHelpDummy(Context context, Fragment fragment) {
        super(
                context,
                android.R.drawable.ic_menu_help,
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
        CharSequence message;
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            message = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
        } else {
            message = Html.fromHtml(content);
        }
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
