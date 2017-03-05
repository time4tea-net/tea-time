package net.time4tea.time.test;

import org.junit.Test;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class FixedClockTest {
    @Test
    public void clockIsAtFixedTime() throws Exception {
        assertThat(FixedClock.atUTC("2017/03/05 16:53:45.123").now().toInstant(), equalTo(Instant.ofEpochMilli(1488732825123L)));
    }
}
