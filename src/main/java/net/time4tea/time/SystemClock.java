package net.time4tea.time;

import java.time.ZonedDateTime;

public class SystemClock implements Clock {
    @Override
    public ZonedDateTime now() {
        return ZonedDateTime.now();
    }
}
