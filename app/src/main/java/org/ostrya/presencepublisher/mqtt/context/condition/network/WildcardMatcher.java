package org.ostrya.presencepublisher.mqtt.context.condition.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Predicate;

import java.util.ArrayList;
import java.util.List;

public class WildcardMatcher implements Predicate<String> {

    private final List<Predicate<String>> rules;

    private WildcardMatcher(List<Predicate<String>> rules) {
        this.rules = rules;
    }

    @Nullable
    public static WildcardMatcher create(@NonNull String input) {
        List<Predicate<String>> rules = new ArrayList<>();
        String[] parts = input.split("\\*", -1);
        if (parts.length == 1) {
            return null;
        }
        int offset = 0;
        if (!parts[0].isEmpty()) {
            rules.add(new StartsWithPredicate(parts[0]));
            offset = parts[0].length();
        }
        for (int i = 1; i + 1 < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                rules.add(new ContainsPredicate(parts[i], offset));
                offset += parts[i].length();
            }
        }
        if (!parts[parts.length - 1].isEmpty()) {
            rules.add(new EndsWithPredicate(parts[parts.length - 1], offset));
        }
        return new WildcardMatcher(rules);
    }

    @Override
    public boolean test(@Nullable String value) {
        if (value == null) {
            return false;
        }
        for (Predicate<String> rule : rules) {
            if (!rule.test(value)) {
                return false;
            }
        }
        return true;
    }

    private static class StartsWithPredicate implements Predicate<String> {
        private final String start;

        private StartsWithPredicate(@NonNull String start) {
            this.start = start;
        }

        @Override
        public boolean test(@NonNull String value) {
            return value.startsWith(start);
        }
    }

    private static class ContainsPredicate implements Predicate<String> {
        private final String content;
        private final int start;

        private ContainsPredicate(@NonNull String content, int start) {
            this.content = content;
            this.start = start;
        }

        @Override
        public boolean test(@NonNull String value) {
            return value.substring(start).contains(content);
        }
    }

    private static class EndsWithPredicate implements Predicate<String> {
        private final String end;
        private final int start;

        private EndsWithPredicate(@NonNull String end, int start) {
            this.end = end;
            this.start = start;
        }

        @Override
        public boolean test(@NonNull String value) {
            return value.substring(start).endsWith(end);
        }
    }
}
