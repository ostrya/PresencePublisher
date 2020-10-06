package org.ostrya.presencepublisher.ui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SetJoiner {
    private SetJoiner() {
        // private constructor for helper class
    }

    public static String join(Set<String> values, String emptyValue) {
        if (values == null || values.isEmpty()) {
            return emptyValue;
        }
        List<String> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : sorted) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
