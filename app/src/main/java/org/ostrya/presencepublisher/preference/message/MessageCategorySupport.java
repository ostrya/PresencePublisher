package org.ostrya.presencepublisher.preference.message;

import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.preference.common.AbstractConfigurationFragment;
import org.ostrya.presencepublisher.preference.common.AbstractDynamicPreferenceCategorySupport;

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
