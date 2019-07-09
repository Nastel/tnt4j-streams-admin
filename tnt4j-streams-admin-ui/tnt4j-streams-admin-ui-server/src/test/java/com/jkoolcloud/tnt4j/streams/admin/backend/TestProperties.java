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

package com.jkoolcloud.tnt4j.streams.admin.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ PropertyData.class })
public class TestProperties {

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Before
	public void setUp() {
		environmentVariables.set(ClsConstants.TOMCAT_HOME_PROPERTY_PATH_NAME,
				ClsConstants.TOMCAT_HOME_PROPERTY_PATH_VALUE);
	}

	// Testing if the data is read and returned from property file correctly
	// Also test if the local and server config files have the same values
	@Test
	public void testStaticGetPropertyMethod() throws IOException {
		Properties props = new Properties();
		InputStream fileIn = this.getClass().getResourceAsStream("/" + ClsConstants.CONFIG_FILE_NAME);
		props.load(fileIn);
		String plannedResult = props.getProperty(ClsConstants.KEY_SERVICE_INFO_NAME);
		String actualResult = PropertyData.getProperty(ClsConstants.KEY_SERVICE_INFO_NAME);
		Assert.assertEquals(plannedResult, actualResult);
	}

	// Testing if the exception will be thrown if the called property will be invalid
	@Test(expected = IllegalArgumentException.class)
	public void testThrowExceptionWhenPropertyNotFound() throws IOException {
		String propFalse = ClsConstants.KEY_SERVICE_INFO_NAME + "False";
		PropertyData.getProperty(propFalse);
	}

}
