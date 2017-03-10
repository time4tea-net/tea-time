package net.time4tea.time;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SystemClock implements Clock {

    private final ZoneId zoneId;

    public SystemClock() {
        this.zoneId = ZoneId.of("UTC");
    }

    public SystemClock(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public ZonedDateTime now() {
        return ZonedDateTime.now(zoneId);
    }
}
