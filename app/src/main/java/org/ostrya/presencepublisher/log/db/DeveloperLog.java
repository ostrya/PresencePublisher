package org.ostrya.presencepublisher.log.db;

import androidx.room.Entity;

@Entity
public class DeveloperLog extends DbLog {
    public DeveloperLog(long id, long timestamp, String line) {
        super(id, timestamp, line);
    }
}
