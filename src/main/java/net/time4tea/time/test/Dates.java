package net.time4tea.time.test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Dates {

    private static final DateTimeFormatter utc = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS").withZone(ZoneId.of("UTC"));

    public static ZonedDateTime utc(String yymmddhhmmssSSS) {
        return ZonedDateTime.from(utc.parse(yymmddhhmmssSSS));
    }
}
