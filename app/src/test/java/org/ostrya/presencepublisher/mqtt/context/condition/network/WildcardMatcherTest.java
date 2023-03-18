package org.ostrya.presencepublisher.mqtt.context.condition.network;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import static java.util.Arrays.asList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class WildcardMatcherTest {
    @Parameters
    public static Iterable<Case> data() {
        return asList(
                new Case("ab*")
                        .withMatching("ab", "abc", "ab*")
                        .withNonMatching("a", "a*", "*", "Ab", "aB", "cab"),
                new Case("a*b")
                        .withMatching("ab", "acb", "a*b")
                        .withNonMatching("a", "a*", "*", "Ab", "aB", "abc"),
                new Case("*ab")
                        .withMatching("ab", "cab", "*ab")
                        .withNonMatching("a", "*a", "*", "Ab", "aB", "abc"),
                new Case("*a*b*")
                        .withMatching("ab", "cab", "acb", "abc", "cacbc", "aaabbbccc")
                        .withNonMatching("a", "ac", "Ab", "aB", "ba"));
    }

    @Parameter public Case testCase;

    @Test
    public void matchesCorrectly() {
        WildcardMatcher matcher = WildcardMatcher.create(testCase.matcher);

        assertSoftly(
                softly -> {
                    softly.assertThat(matcher).isNotNull();
                    testCase.matchingStrings.forEach(
                            s ->
                                    softly.assertThat(matcher.test(s))
                                            .as(testCase.matcher + " should match " + s)
                                            .isTrue());
                    testCase.nonMatchingStrings.forEach(
                            s ->
                                    softly.assertThat(matcher.test(s))
                                            .as(testCase.matcher + " should not match " + s)
                                            .isFalse());
                });
    }

    private static class Case {
        private final String matcher;
        private final List<String> matchingStrings = new ArrayList<>();
        private final List<String> nonMatchingStrings = new ArrayList<>();

        private Case(String matcher) {
            this.matcher = matcher;
        }

        private Case withMatching(String... matching) {
            matchingStrings.addAll(Arrays.asList(matching));
            return this;
        }

        private Case withNonMatching(String... nonMatching) {
            nonMatchingStrings.addAll(Arrays.asList(nonMatching));
            return this;
        }
    }
}
