package org.ostrya.presencepublisher.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ostrya.presencepublisher.message.MessageFormat.CSV;
import static org.ostrya.presencepublisher.message.MessageFormat.JSON;
import static org.ostrya.presencepublisher.message.MessageFormat.PLAINTEXT;
import static org.ostrya.presencepublisher.message.MessageFormat.YAML;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(Parameterized.class)
public class MessageFormatTest {
    @Parameterized.Parameter public TestCase testCase;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {
                        new TestCase("single string entry")
                                .withEntry(new StringEntry("name", "value"))
                                .withExpectedMessages(PLAINTEXT, "value")
                                .withExpectedMessages(CSV, "\"name=value\"")
                                .withExpectedMessages(JSON, "{\n  \"name\": \"value\"\n}")
                                .withExpectedMessages(YAML, "name: value")
                    },
                    {
                        new TestCase("single number entry")
                                .withEntry(new NumberEntry("name", 123))
                                .withExpectedMessages(PLAINTEXT, "123")
                                .withExpectedMessages(CSV, "\"name=123\"")
                                .withExpectedMessages(JSON, "{\n  \"name\": 123\n}")
                                .withExpectedMessages(YAML, "name: 123")
                    },
                    {
                        new TestCase("single list entry with one element")
                                .withEntry(
                                        new ListEntry("name", Collections.singletonList("value")))
                                .withExpectedMessages(PLAINTEXT, "value")
                                .withExpectedMessages(CSV, "\"name=value\"")
                                .withExpectedMessages(JSON, "{\n  \"name\": [\"value\"]\n}")
                                .withExpectedMessages(YAML, "name: [value]")
                    },
                    {
                        new TestCase("single list entry with two elements")
                                .withEntry(new ListEntry("name", Arrays.asList("value1", "value2")))
                                .withExpectedMessages(PLAINTEXT, "value1", "value2")
                                .withExpectedMessages(CSV, "\"name=value1\",\"name=value2\"")
                                .withExpectedMessages(
                                        JSON, "{\n  \"name\": [\"value1\",\"value2\"]\n}")
                                .withExpectedMessages(YAML, "name: [value1,value2]")
                    },
                    {
                        new TestCase("one entry of each type")
                                .withEntry(new StringEntry("s_name", "s_value"))
                                .withEntry(new NumberEntry("n_name", 123))
                                .withEntry(
                                        new ListEntry(
                                                "l_name", Collections.singletonList("l_value")))
                                .withExpectedMessages(PLAINTEXT, "s_value", "123", "l_value")
                                .withExpectedMessages(
                                        CSV, "\"s_name=s_value\",\"n_name=123\",\"l_name=l_value\"")
                                .withExpectedMessages(
                                        JSON,
                                        "{\n"
                                                + "  \"s_name\": \"s_value\",\n"
                                                + "  \"n_name\": 123,\n"
                                                + "  \"l_name\": [\"l_value\"]\n"
                                                + "}")
                                .withExpectedMessages(
                                        YAML, "s_name: s_value\nn_name: 123\nl_name: [l_value]")
                    }
                });
    }

    @Test
    public void formatContent_works() {
        assertThat(
                        PLAINTEXT.formatContent(
                                testCase.stringEntries,
                                testCase.numberEntries,
                                testCase.listEntries))
                .containsExactlyElementsOf(testCase.expectedPlaintextMessages);
        assertThat(
                        CSV.formatContent(
                                testCase.stringEntries,
                                testCase.numberEntries,
                                testCase.listEntries))
                .containsExactlyElementsOf(testCase.expectedCsvMessages);
        assertThat(
                        JSON.formatContent(
                                testCase.stringEntries,
                                testCase.numberEntries,
                                testCase.listEntries))
                .containsExactlyElementsOf(testCase.expectedJsonMessages);
        assertThat(
                        YAML.formatContent(
                                testCase.stringEntries,
                                testCase.numberEntries,
                                testCase.listEntries))
                .containsExactlyElementsOf(testCase.expectedYamlMessages);
    }

    private static class TestCase {
        private final String description;
        private final List<StringEntry> stringEntries = new ArrayList<>();
        private final List<NumberEntry> numberEntries = new ArrayList<>();
        private final List<ListEntry> listEntries = new ArrayList<>();

        private final List<String> expectedPlaintextMessages = new ArrayList<>();
        private final List<String> expectedCsvMessages = new ArrayList<>();
        private final List<String> expectedJsonMessages = new ArrayList<>();
        private final List<String> expectedYamlMessages = new ArrayList<>();

        private TestCase(String description) {
            this.description = description;
        }

        public TestCase withEntry(StringEntry entry) {
            stringEntries.add(entry);
            return this;
        }

        public TestCase withEntry(NumberEntry entry) {
            numberEntries.add(entry);
            return this;
        }

        public TestCase withEntry(ListEntry entry) {
            listEntries.add(entry);
            return this;
        }

        public TestCase withExpectedMessages(
                MessageFormat messageFormat, String... expectedMessages) {
            switch (messageFormat) {
                case PLAINTEXT:
                    expectedPlaintextMessages.addAll(Arrays.asList(expectedMessages));
                    break;
                case CSV:
                    expectedCsvMessages.addAll(Arrays.asList(expectedMessages));
                    break;
                case JSON:
                    expectedJsonMessages.addAll(Arrays.asList(expectedMessages));
                    break;
                case YAML:
                    expectedYamlMessages.addAll(Arrays.asList(expectedMessages));
                    break;
            }
            return this;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
