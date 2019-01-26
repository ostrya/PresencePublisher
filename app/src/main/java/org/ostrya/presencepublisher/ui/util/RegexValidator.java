package org.ostrya.presencepublisher.ui.util;

import java.util.regex.Pattern;

public class RegexValidator implements Validator {
    private final Pattern pattern;

    public RegexValidator(final String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean isValid(String value) {
        return pattern.matcher(value).matches();
    }
}
