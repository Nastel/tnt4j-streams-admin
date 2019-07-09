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

package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

import com.jkoolcloud.tnt4j.core.OpLevel;
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

	static{
		 streamsAdminLogger = LoggerUtils.getLoggerSink("streamsAdminLogger");
		 //streamsAdminLogger.setEventFormatter(new DefaultFormatter("{2}")); // NON-NLS
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

}
