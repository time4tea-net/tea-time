package net.time4tea.time;

import org.junit.Test;

import java.time.ZoneId;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SystemClockTest {

    @Test
    public void defaultsToUTC() throws Exception {
        assertThat(new SystemClock().now().getZone(), equalTo(ZoneId.of("UTC")));
    }

    @Test
    public void canChooseAZone() throws Exception {
        assertThat(new SystemClock(ZoneId.of("Europe/London")).now().getZone(), equalTo(ZoneId.of("Europe/London")));
    }
}