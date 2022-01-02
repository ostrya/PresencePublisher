package org.ostrya.presencepublisher.ui.log;

import org.ostrya.presencepublisher.R;

public enum LogType {
    DEVELOPER,
    DETECTION,
    MESSAGES;

    /**
     * Note: the values returned here must match with the descriptions given in {@link
     * #settingDescriptions()}
     */
    public static LogType[] settingValues() {
        return new LogType[] {
            MESSAGES, DETECTION, DEVELOPER,
        };
    }

    public static int settingDescriptions() {
        return R.array.log_type_descriptions;
    }
}
