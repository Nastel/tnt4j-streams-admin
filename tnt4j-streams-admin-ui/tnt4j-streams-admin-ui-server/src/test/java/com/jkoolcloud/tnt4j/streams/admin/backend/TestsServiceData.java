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

import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;

@RunWith(MockitoJUnitRunner.class)
public class TestsServiceData {

	private ServiceData serviceData = new ServiceData();
	private ServiceData serviceDataSpy;

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Before
	public void setUp() {
		serviceDataSpy = spy(serviceData);
		environmentVariables.set(ClsConstants.TOMCAT_HOME_PROPERTY_PATH_NAME,
				ClsConstants.TOMCAT_HOME_PROPERTY_PATH_VALUE);
	}

	// Changed with private method mocking added stability changing config file will not ruin the test case \
	// Can be found in TestsPowerMockServiceData
	@Test
	public void testForParsingToJson() throws IOException, JSONException {
		String plannedResult = "{\"eth\":{\"ping.jsp\":\" pong\",\"JKoolHealthCheck\":true,\"InfuraHealthCheck\":true}}";
		String actualResult = serviceDataSpy.serviceInfoParseToJSON("eth", false);
		JSONAssert.assertEquals(plannedResult, actualResult, JSONCompareMode.LENIENT);
	}

	@Test
	public void testETH() throws IOException {
		ServiceData readAndParseData = new ServiceData();
		System.out.println("test");
		System.out.println(readAndParseData.serviceInfoParseToJSON("eth", false));
	}

}