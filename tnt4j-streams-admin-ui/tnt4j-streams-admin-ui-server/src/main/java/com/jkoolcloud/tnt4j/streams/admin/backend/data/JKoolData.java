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
 * The type J kool data.
 */
public class JKoolData {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceData.class);

	/**
	 * Instantiates a new J kool data.
	 */
	public JKoolData() {
	}

	/**
	 * Gets data from incomplete blocks.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the data from incomplete blocks
	 */
	public static String getDataFromIncompleteBlocks(String serviceName) {
		String urlLink = buildLinkURL(serviceName, "incompleteBlocksStart", "incompleteBlocksEnd");
		String errLog = readURL(urlLink);
		return errLog;
	}

	/**
	 * Gets data from incomplete blocks no receipt.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the data from incomplete blocks no receipt
	 */
	public static String getDataFromIncompleteBlocksNoReceipt(String serviceName) {
		String urlLink = buildLinkURL(serviceName, "incompleteBlocksNoReceiptStart", "incompleteBlocksNoReceiptEnd");
		String errLog = readURL(urlLink);
		return errLog;
	}

	/**
	 * Gets data from repository.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the data from repository
	 */
	public static String getDataFromRepository(String serviceName) {
		String urlLink = buildLinkURL(serviceName, "repositoryDataStart", "repositoryDataEnd");
		String errLog = readURL(urlLink);
		return errLog;
	}

	private static String buildLinkURL(String serviceName, String start, String end) {
		String linkAddress = "";
		try {
			linkAddress = PropertyData.getProperty(start)
					+ PropertyData.getProperty(serviceName + ClsConstants.STREAM_TOKEN_NAME)
					+ PropertyData.getProperty(end);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return linkAddress;
	}

	/**
	 * Read url string.
	 *
	 * @param serviceLink
	 *            the service link
	 * @return the string
	 */
	public static String readURL(String serviceLink) {
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

		return response;
	}
}
