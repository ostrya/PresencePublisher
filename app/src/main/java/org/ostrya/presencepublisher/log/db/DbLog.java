package org.ostrya.presencepublisher.log.db;

import org.ostrya.presencepublisher.ui.log.LogItem;

public interface DbLog extends LogItem {
    long getTimestamp();
}
