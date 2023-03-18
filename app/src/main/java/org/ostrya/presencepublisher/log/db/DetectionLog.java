package org.ostrya.presencepublisher.log.db;

import androidx.room.Entity;

@Entity
public class DetectionLog extends DbLog {
    public DetectionLog(long id, long timestamp, String line) {
        super(id, timestamp, line);
    }
}
