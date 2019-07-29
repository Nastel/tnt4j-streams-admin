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

package com.jkoolcloud.tnt4j.streams.admin.backend.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jkoolcloud.tnt4j.streams.admin.backend.ServiceData;

/**
 * The type Property dataReading.
 */
public class PropertyData {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceData.class);

	/**
	 * Instantiates a new Property dataReading.
	 */
	private PropertyData() {
	}

	/**
	 * Gets property.
	 *
	 * @param key
	 *            the key
	 * @return the property
	 * @throws IOException
	 *             the io exception
	 */
	// Gets the dataReading from property file or throws an exception if property key does not exist
	public static String getProperty(String key) throws IOException {
		// LOG.info("the property key: | " + key +" |");

		ResourceBundle rb = ResourceBundle.getBundle("serviceInfoLinksCfg");
		String property;
		InputStream inputStream;
		String propFileName;

		if (System.getenv(ClsConstants.TOMCAT_HOME_PROPERTY_PATH_NAME) == null) {
			// For local config file
			if (System.getProperty(ClsConstants.TOMCAT_HOME_PROPERTY_PATH_NAME) == null) {
				property = rb.getString(key);
			}
			// For Unit testing
			else {
				propFileName = System.getProperty(ClsConstants.TOMCAT_HOME_PROPERTY_PATH_NAME) + File.separator
						+ ClsConstants.CONFIG_FILE_PATH + File.separator + ClsConstants.CONFIG_FILE_NAME;
				Properties prop = new Properties();
				inputStream = new FileInputStream(propFileName);
				prop.load(inputStream);
				property = prop.getProperty(key);
				inputStream.close();
			}
		}
		// For server config file
		else {
			propFileName = System.getenv(ClsConstants.TOMCAT_HOME_PROPERTY_PATH_NAME) + File.separator
					+ ClsConstants.CONFIG_FILE_PATH + File.separator + ClsConstants.CONFIG_FILE_NAME;
			Properties prop = new Properties();
			inputStream = new FileInputStream(propFileName);
			prop.load(inputStream);
			property = prop.getProperty(key);

			inputStream.close();
		}
		if (property == null) {
			LOG.error(
					"The property provided has wrong value or was not declared correctly in the serviceInfoLinksCfg.properties file: "
							+ key);
			throw new IllegalArgumentException(
					MessageFormat.format("\"BAD REQUEST \"+ Missing value for key {0}!", key));
		}

		return property;
	}


	public static List<String> splitPropertiesList(String propertySet, String separator) {
		List<String> propertyData = new LinkedList<>();
		try{
			propertyData = Arrays.asList(propertySet.split(separator));

		}catch(Throwable e){
			LOG.error("Problem on separating properties list:  {}!", propertySet);
			e.printStackTrace();
		}
		return propertyData;
	}



}