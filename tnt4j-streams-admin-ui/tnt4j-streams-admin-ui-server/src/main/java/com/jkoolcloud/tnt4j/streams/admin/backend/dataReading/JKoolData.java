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

package com.jkoolcloud.tnt4j.streams.admin.backend.dataReading;

import java.io.IOException;

import javax.inject.Singleton;

import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.HttpUtils;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;

/**
 * The type J kool dataReading.
 */
@Singleton
public class JKoolData {

	/**
	 * Instantiates a new J kool dataReading.
	 */
	public JKoolData() {
	}

	/**
	 * Gets dataReading from incomplete blocks.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the dataReading from incomplete blocks
	 */
	public static String getDataFromIncompleteBlocks(String serviceName) {
		String urlLink = buildLinkURL(serviceName, "incompleteBlocksStart", "incompleteBlocksEnd");
		String errLog = HttpUtils.readURLData(urlLink);
		return errLog;
	}

	/**
	 * Gets dataReading from incomplete blocks no receipt.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the dataReading from incomplete blocks no receipt
	 */
	public static String getDataFromIncompleteBlocksNoReceipt(String serviceName) {
		String urlLink = buildLinkURL(serviceName, "incompleteBlocksNoReceiptStart", "incompleteBlocksNoReceiptEnd");
		String errLog = HttpUtils.readURLData(urlLink);
		return errLog;
	}

	/**
	 * Gets dataReading from repository.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the dataReading from repository
	 */
	public static String getDataFromRepository(String serviceName) {
		String urlLink = buildLinkURL(serviceName, "repositoryDataStart", "repositoryDataEnd");
		String errLog = HttpUtils.readURLData(urlLink);
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
}
