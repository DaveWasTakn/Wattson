package com.dave.Util;

import java.time.Duration;
import java.time.Instant;

public class Utils {

    public static String dateTime() {
        return Instant.now().toString();
    }

    public static String dateTimePlusMinutes(long minutes) {
        return Instant.now().plus(Duration.ofMinutes(minutes)).toString();
    }

}
