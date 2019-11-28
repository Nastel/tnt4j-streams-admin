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

import static org.powermock.api.mockito.PowerMockito.spy;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({ ServiceData.class })
public class TestsPowerMockServiceData {

	private ServiceData serviceDataSpy;
	private ArrayList<String> mockData;

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Before
	public void setUp() {
		environmentVariables.set(ClsConstants.TOMCAT_HOME_PROPERTY_PATH_NAME,
				ClsConstants.TOMCAT_HOME_PROPERTY_PATH_VALUE);
		ServiceData serviceData = new ServiceData();
		serviceDataSpy = spy(serviceData);
		mockData = new ArrayList<>();
	}
	/*
	 * @Test public void testIfNotJsonDataIsParsedToJson() throws Exception { String plannedResult =
	 * "{\"eth\":{\"ping.jsp\":\"pong\"}}"; mockData.add("https://www.gocypher.com/eth_info/ping.jsp");
	 * doReturn(mockData).when(serviceDataSpy ,"createLinksToServiceData", anyString());
	 * doReturn("pong").when(serviceDataSpy, "readURL", "https://www.gocypher.com/eth_info/ping.jsp"); String
	 * actualResult = serviceDataSpy.serviceInfoParseToJSON("eth", false); JSONAssert.assertEquals(plannedResult,
	 * actualResult, JSONCompareMode.LENIENT); }
	 * 
	 * @Test public void testIfJsonDataIsParsedToJson() throws Exception { String plannedResult =
	 * "{\"eth\":{\"JKoolHealthCheck\":true,\"InfuraHealthCheck\":true}}";
	 * mockData.add("https://www.gocypher.com/eth_info/health.jsp"); doReturn(mockData).when(serviceDataSpy
	 * ,"createLinksToServiceData", "eth");
	 * doReturn("{\"InfuraHealthCheck\":{\"healthy\":true},\"JKoolHealthCheck\":{\"healthy\":true}}").when(
	 * serviceDataSpy, "readURL", "https://www.gocypher.com/eth_info/health.jsp"); String actualResult =
	 * serviceDataSpy.serviceInfoParseToJSON("eth", false); JSONAssert.assertEquals(plannedResult, actualResult,
	 * JSONCompareMode.LENIENT); }
	 */

}
