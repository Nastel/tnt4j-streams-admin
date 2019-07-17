package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

import java.time.Clock;
import java.time.LocalDateTime;

public class TimeUtils {

	public static LocalDateTime getCurrentTime(Clock clock) {
		LocalDateTime localDateTime = LocalDateTime.now(clock);

		return localDateTime;
	}

	public static String getCurrentTimeStr(String timeZone) {
		return getCurrentTime(Clock.systemUTC()).toString() + "[UTC]";
	}

}
