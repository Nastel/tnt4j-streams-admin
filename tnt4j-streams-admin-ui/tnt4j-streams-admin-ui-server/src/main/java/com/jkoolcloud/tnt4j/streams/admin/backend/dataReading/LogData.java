/*
 * Copyright 2014-2020 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.streams.admin.backend.dataReading;

import java.io.IOException;

import javax.inject.Singleton;

import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.HttpUtils;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;

/**
 * The type Log dataReading.
 */
@Singleton
public class LogData {
	/**
	 * Instantiates a new Log dataReading.
	 */
	public LogData() {
	}

	/**
	 * Gets dataReading from error logs.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the dataReading from error logs
	 * @throws IOException
	 *             the io exception
	 */
	public String getDataFromErrorLogs(String serviceName) throws IOException {
		String urlLink = PropertyData.getProperty(serviceName + ClsConstants.ERROR_LOG_ENDPOINT_CONFIG);
		String errLog = HttpUtils.readURLData(urlLink);
		return errLog;
	}

	/**
	 * Gets dataReading from logs.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the dataReading from logs
	 * @throws IOException
	 *             the io exception
	 */
	public String getDataFromLogs(String serviceName) throws IOException {
		String urlLink = PropertyData.getProperty(serviceName + ClsConstants.LOG_ENDPOINT_CONFIG);
		String log = checkResponse(HttpUtils.readURLData(urlLink));
		return log;
	}

	private String checkResponse(String data) {
		if (data.length() > 3) {
			data = data.substring(1, data.length() - 3);
		} else if (data.length() <= 3) {
			data = "No logs dataReading loaded";
		}
		return data;
	}
}
