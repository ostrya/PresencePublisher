package org.ostrya.presencepublisher.log.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DetectionLog implements DbLog {
    @PrimaryKey(autoGenerate = true)
    private final long id;

    private final long timestamp;
    private final String line;

    public DetectionLog(long id, long timestamp, String line) {
        this.id = id;
        this.timestamp = timestamp;
        this.line = line;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getLine() {
        return line;
    }
}
