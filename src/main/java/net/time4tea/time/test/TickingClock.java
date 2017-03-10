package net.time4tea.time.test;

import net.time4tea.time.Clock;

import java.time.Duration;
import java.time.ZonedDateTime;

public class TickingClock implements Clock {

    private ZonedDateTime now;

    public TickingClock(ZonedDateTime startTime) {
        now = startTime;
    }

    @Override
    public ZonedDateTime now() {
        return now;
    }

    public static TickingClock atUTC(String yymmddhhmmssSSS) {
        return new TickingClock(Dates.utc(yymmddhhmmssSSS));
    }

    public void tick(Duration duration) {
        if ( duration.isNegative() ) {
            throw new IllegalArgumentException("clocks can only tick forwards, not by " + duration);
        }
        now = now.plus(duration);
    }
}
