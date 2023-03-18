package org.ostrya.presencepublisher.log;

import androidx.annotation.NonNull;

public class LogItem {
    private final long id;
    private final String line;

    public LogItem(long id, @NonNull String line) {
        this.id = id;
        this.line = line;
    }

    public long getId() {
        return id;
    }

    @NonNull
    public String getLine() {
        return line;
    }
}
