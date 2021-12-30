package org.ostrya.presencepublisher.ui.util;

public class RangeValidator implements Validator {
    private final long min;
    private final long max;

    public RangeValidator(long min, long max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isValid(String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        long parsed;
        try {
            parsed = Long.parseLong(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return parsed >= min && parsed <= max;
    }
}
