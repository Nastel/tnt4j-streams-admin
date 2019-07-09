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

package com.jkoolcloud.tnt4j.streams.admin.backend.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jkoolcloud.tnt4j.streams.admin.backend.ServiceData;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;

/**
 * The type Log data.
 */
public class LogData {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceData.class);

	/**
	 * Instantiates a new Log data.
	 */
	public LogData() {
	}

	/**
	 * Gets data from error logs.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the data from error logs
	 * @throws IOException
	 *             the io exception
	 */
	public static String getDataFromErrorLogs(String serviceName) throws IOException {
		String urlLink = PropertyData.getProperty(serviceName + ClsConstants.ERROR_LOG_ENDPOINT_CONFIG);
		String errLog = readURL(urlLink);
		return errLog;
	}

	/**
	 * Gets data from logs.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the data from logs
	 * @throws IOException
	 *             the io exception
	 */
	public static String getDataFromLogs(String serviceName) throws IOException {
		String urlLink = PropertyData.getProperty(serviceName + ClsConstants.LOG_ENDPOINT_CONFIG);
		String log = readURL(urlLink);
		return log;
	}

	/**
	 * Read url string.
	 *
	 * @param serviceLink
	 *            the service link
	 * @return the string
	 * @throws IOException
	 *             the io exception
	 */
	public static String readURL(String serviceLink) throws IOException {
		String response = "";

		try {
			URL url = new URL(serviceLink);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

			while (true) {
				String line;
				if ((line = reader.readLine()) == null) {
					reader.close();
					break;
				}

				line = line.replaceFirst("^,", "");
				response = response + line;
				response = response + "\n";
			}
		} catch (Exception var5) {
			LOG.error("The link provided " + serviceLink + " was wrong or can not be accessed at the moment");
			throw new NotFoundException("client connection to " + serviceLink + " fail: no connection");
		}

		if (response.length() > 3) {
			response = response.substring(1, response.length() - 3);
		} else if (response.length() <= 3) {
			response = "No logs data loaded";
		}

		return response;
	}
}
