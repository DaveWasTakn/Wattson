package com.dave.Main.Util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Utils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String dateTime() {
        return Instant.now().toString();
    }

    public static String dateTimePlusMinutes(long minutes) {
        return Instant.now().plus(Duration.ofMinutes(minutes)).toString();
    }

    public static String formatEpochSeconds(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime.format(FORMATTER);
    }


}
