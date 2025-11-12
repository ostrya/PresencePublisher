package org.ostrya.presencepublisher.log;

import java.util.Locale;

public class FormatArgs {
    private final Object[] args;

    public FormatArgs(Object[] args) {
        this.args = args;
    }

    public static FormatArgs args(Object... args) {
        return new FormatArgs(args);
    }

    public String applyTo(String format) {
        return String.format(Locale.ROOT, format, args);
    }
}
