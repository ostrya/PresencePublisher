package org.ostrya.presencepublisher.message;

import java.util.List;

public class ListEntry {
    private final String name;
    private final List<String> values;

    public ListEntry(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }
}
