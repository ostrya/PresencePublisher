package org.ostrya.presencepublisher.preference.condition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Predicate;

import org.ostrya.presencepublisher.mqtt.context.condition.network.WildcardMatcher;

import java.util.Objects;

public class WifiNetwork {
    private static final String WILDCARD_FLAG = "\0";

    @NonNull private final String name;

    @Nullable private final Predicate<String> wildcardMatcher;

    private WifiNetwork(@NonNull String name, boolean useWildcard) {
        this.name = Objects.requireNonNull(name);
        if (useWildcard) {
            wildcardMatcher = WildcardMatcher.create(name);
        } else {
            wildcardMatcher = null;
        }
    }

    @NonNull
    public static String toRawString(@NonNull String name, boolean useWildcard) {
        return useWildcard ? WILDCARD_FLAG + name : name;
    }

    @Nullable
    public static WifiNetwork fromRawString(@Nullable String rawString) {
        if (rawString == null) {
            return null;
        }
        if (rawString.startsWith(WILDCARD_FLAG)) {
            return new WifiNetwork(rawString.substring(1), true);
        } else {
            return new WifiNetwork(rawString, false);
        }
    }

    @NonNull
    public String getName() {
        return name;
    }

    public String getRawString() {
        return toRawString(name, hasWildcard());
    }

    public boolean hasWildcard() {
        return wildcardMatcher != null;
    }

    public boolean matches(@Nullable String currentSsid) {
        if (currentSsid == null) {
            return false;
        }
        if (wildcardMatcher != null) {
            return wildcardMatcher.test(currentSsid);
        }
        return name.equals(currentSsid);
    }
}
