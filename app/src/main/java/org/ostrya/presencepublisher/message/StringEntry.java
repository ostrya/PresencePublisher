package org.ostrya.presencepublisher.message;

public class StringEntry {
    private final String name;
    private final String value;

    public StringEntry(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
