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

package com.jkoolcloud.tnt4j.streams.registry.zoo.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.format.DefaultFormatter;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.utils.LoggerUtils;

/**
 * The type Logger wrapper.
 */
public class LoggerWrapper {

	/**
	 * The Event sink.
	 */
	private static final EventSink streamsAdminLogger;

	static {
		streamsAdminLogger = LoggerUtils.getLoggerSink("streamsAdmin_error");
		streamsAdminLogger.setEventFormatter(new DefaultFormatter("{2}")); // NON-NLS
	}

	/**
	 * Add message.
	 *
	 * @param opLevel
	 *            the op level
	 * @param msg
	 *            the msg
	 */
	public static void addMessage(OpLevel opLevel, String msg) {
		streamsAdminLogger.log(opLevel, msg);
	}

	public static void addQuartzJobLog(String clazzName, String zkPath, String payload) {
		LoggerWrapper.addMessage(OpLevel.ERROR, String.format("%1$-20s: %2$20s", "failed", clazzName));
		LoggerWrapper.addMessage(OpLevel.ERROR, String.format("%1$-20s: %2$30s", "Path:", zkPath));
		// LoggerWrapper.addMessage(OpLevel.ERROR, String.format("%1$-20s: %2$30s","Response", payload ));
		LoggerWrapper.addMessage(OpLevel.ERROR,
				String.format("%1$-20s: %2$3d", "ResponseSizeInBytes", payload.getBytes().length));
	}

	private static String getStatTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		e.printStackTrace(pw);

		String stackTrace = sw.toString();

		pw.close();

		return stackTrace;
	}

	public static void logStackTrace(OpLevel opLevel, Exception e) {
		LoggerWrapper.addMessage(opLevel, getStatTrace(e));
	}

	public static void closeLogger() throws IOException {
		streamsAdminLogger.close();
	}
}
