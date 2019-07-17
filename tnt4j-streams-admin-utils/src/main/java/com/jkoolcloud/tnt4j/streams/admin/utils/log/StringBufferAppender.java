/*
 * Copyright 2014-2019 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkoolcloud.tnt4j.streams.admin.utils.log;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * The type String buffer appender.
 */
public class StringBufferAppender extends AppenderSkeleton {
	// StringBuffer logs = new StringBuffer();

	/**
	 * The Logs.
	 */
	ArrayList<String> logs = new ArrayList<>();

	/**
	 * The Capture mode.
	 */
	AtomicBoolean captureMode = new AtomicBoolean(false);
	private ByteArrayOutputStream outStream;
	/**
	 * The Out buf size.
	 */
	static final int OUT_BUF_SIZE = 400;

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean requiresLayout() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {

		String date = formatDate(event.getTimeStamp());
		String logLine = date + "| " + event.getLevel() + " |" + event.getLoggerName() + "| =>  " + event.getMessage()
				+ "\r\n";

		int newLogLength = logs.size();
		if (OUT_BUF_SIZE > logs.size()) {
			logs.add(logLine);
		} else {
			logs.remove(0);
			logs.add(logLine);
		}

	}

	private String formatDate(long dateEvent) {
		Timestamp ts = new Timestamp(dateEvent);
		Date date = new Date(ts.getTime());
		DateFormat f = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss,mmm");
		f.setTimeZone(TimeZone.getTimeZone("UTC"));
		return f.format(date);
	}

	/**
	 * Start.
	 */
	public void start() {
		StringBuilder logs = new StringBuilder();
		captureMode.set(true);
	}

	/**
	 * Stop array list.
	 *
	 * @return the array list
	 */
	public ArrayList<String> stop() {
		captureMode.set(false);
		ArrayList<String> data = new ArrayList<>(logs);
		logs = null;
		return data;
	}

	/**
	 * Gets logs.
	 *
	 * @return the logs
	 */
	public ArrayList<String> getLogs() {
		return logs;
	}
}
