package net.time4tea.time.test;

import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TickingClockTest {
    @Test
    public void clockInitialisedAtCorrectTime() throws Exception {
        assertThat(TickingClock.atUTC("2017/03/05 16:53:45.123").now().toInstant(), equalTo(Instant.ofEpochMilli(1488732825123L)));
    }

    @Test
    public void clockCanBeTickedForward() throws Exception {
        TickingClock clock = TickingClock.atUTC("2017/03/05 16:53:45.123");
        clock.tick(Duration.ofSeconds(1));
        assertThat(clock.now().toInstant(), equalTo(Instant.ofEpochMilli(1488732826123L)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void clockCannotBeTickedBack() throws Exception {
        TickingClock clock = TickingClock.atUTC("2017/03/05 16:53:45.123");
        clock.tick(Duration.ofSeconds(-1));
    }
}
