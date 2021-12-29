package org.ostrya.presencepublisher.util;

import java.util.Arrays;

public class ObjectUtil {
    /**
     * as Objects.hash is only available starting with API level 19, we need to provide our own
     * implementation
     */
    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }
}
