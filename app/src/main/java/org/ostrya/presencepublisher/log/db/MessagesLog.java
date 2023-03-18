package org.ostrya.presencepublisher.log.db;

import androidx.room.Entity;

@Entity
public class MessagesLog extends DbLog {
    public MessagesLog(long id, long timestamp, String line) {
        super(id, timestamp, line);
    }
}
