package org.ostrya.presencepublisher.ui.preference.messages;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.ui.preference.common.AbstractDynamicPreferenceCategorySupport;
import org.ostrya.presencepublisher.ui.util.AbstractConfigurationFragment;

public class MessageCategorySupport extends AbstractDynamicPreferenceCategorySupport {
    public static final String MESSAGE_LIST = "messages";
    public static final String MESSAGE_CONFIG_PREFIX = "message.";

    public MessageCategorySupport(AbstractConfigurationFragment fragment) {
        super(
                fragment,
                R.string.category_messages,
                MESSAGE_LIST,
                MESSAGE_CONFIG_PREFIX,
                AddMessageChoicePreferenceDummy::new,
                MessagePreference::new);
    }
}
