package org.ostrya.presencepublisher.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ostrya.presencepublisher.log.FormatArgs.args;

import org.junit.Test;

public class FormatArgsTest {
    @Test
    public void formatting_works() {
        assertThat(args().applyTo("no formatting expected")).isEqualTo("no formatting expected");
        assertThat(args("foo", 1, true).applyTo("[%s] %d - '%s'")).isEqualTo("[foo] 1 - 'true'");
    }
}
