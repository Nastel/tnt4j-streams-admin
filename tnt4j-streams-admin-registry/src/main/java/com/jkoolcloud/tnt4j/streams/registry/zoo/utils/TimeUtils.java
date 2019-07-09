package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtils {


    public static ZonedDateTime getCurrentTime(String timeZone){
        LocalDateTime localDateTime = LocalDateTime.now();
        ZoneId zoneId = ZoneId.of(timeZone);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);

        return zonedDateTime;
    }

    public static String getCurrentTimeStr(String timeZone){
        return getCurrentTime(timeZone).toString();
    }

}
