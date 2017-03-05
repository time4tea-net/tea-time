package net.time4tea.time.test;

import net.time4tea.time.Clock;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicReference;

public class TickingClock implements Clock {

    private final AtomicReference<ZonedDateTime> now;

    public TickingClock(ZonedDateTime startTime) {
        now = new AtomicReference<>(startTime);
    }

    @Override
    public ZonedDateTime now() {
        return now.get();
    }

    public static TickingClock atUTC(String yymmddhhmmssSSS) {
        return new TickingClock(Dates.utc(yymmddhhmmssSSS));
    }

    public void tick(Duration duration) {
        now.set(
                now.get().plus(duration)
        );
    }
}
