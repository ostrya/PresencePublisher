package org.ostrya.presencepublisher.mqtt.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class MessageItemTest {
    @Parameterized.Parameter(0)
    public String input;

    @Parameterized.Parameter(1)
    public String expectedOutput;

    @Parameterized.Parameters(name = "{index}: {0} should become {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {"", ""},
                    {"_", ""},
                    {"__", ""},
                    {"foo", "foo"},
                    {"foO", "foo"},
                    {"fOO", "foo"},
                    {"FoO", "foo"},
                    {"FOO", "foo"},
                    {"fOo", "foo"},
                    {"FOo", "foo"},
                    {"Foo", "foo"},
                    {"FOO_BAR", "fooBar"},
                    {"_FOO_BAR", "FooBar"},
                    {"FOO__BAR", "fooBar"},
                    {"FOO_BAR_", "fooBar"},
                });
    }

    @Test
    public void toCamelCase_works() {
        assertThat(MessageItem.toCamelCase(input)).isEqualTo(expectedOutput);
    }
}
