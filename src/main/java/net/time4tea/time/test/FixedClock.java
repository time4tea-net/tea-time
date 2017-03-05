package net.time4tea.time.test;

import net.time4tea.time.Clock;

import java.time.ZonedDateTime;

public class FixedClock implements Clock {

    private final ZonedDateTime theNow;

    public FixedClock(ZonedDateTime theNow) {
        this.theNow = theNow;
    }

    @Override
    public ZonedDateTime now() {
        return theNow;
    }

    public static FixedClock atUTC(String yymmddhhmmss) {
        return new FixedClock(Dates.utc(yymmddhhmmss));
    }

    public static FixedClock at(ZonedDateTime dateTime) {
        return new FixedClock(dateTime);
    }
}
