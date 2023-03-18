package org.ostrya.presencepublisher.log.db;

import androidx.room.PrimaryKey;

public class DbLog {
    @PrimaryKey(autoGenerate = true)
    private final long id;

    private final long timestamp;
    private final String line;

    protected DbLog(long id, long timestamp, String line) {
        this.id = id;
        this.timestamp = timestamp;
        this.line = line;
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLine() {
        return line;
    }
}
