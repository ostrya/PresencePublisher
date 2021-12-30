package org.ostrya.presencepublisher.message;

public class NumberEntry {
    private final String name;
    private final Number value;

    public NumberEntry(String name, Number value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Number getValue() {
        return value;
    }
}
