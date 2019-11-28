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

import static org.mockito.Mockito.mock;

import java.net.URISyntaxException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.Dispatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.HttpUtils;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ ServiceData.class })
public class TestsServiceDataEndpointsResponse {
	private static Dispatcher dispatcher;

	// This code here gets run before our tests begin
	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setup() {
		environmentVariables.set(ClsConstants.TOMCAT_HOME_PROPERTY_PATH_NAME,
				ClsConstants.TOMCAT_HOME_PROPERTY_PATH_VALUE);
		dispatcher = MockDispatcherFactory.createDispatcher();
		ServiceData serviceData = mock(ServiceData.class);
		ServiceInfoEndpoint serviceEndpoint = new ServiceInfoEndpoint();
		serviceEndpoint.setServiceData(serviceData);
		dispatcher.getRegistry().addSingletonResource(serviceEndpoint);
	}

	@Test
	public void testResponseFromCorrectEndpoint() throws Exception {
		MockHttpResponse response = sendAsyncPostRequest("/health_services/all", "");
		Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	@Test
	public void testResponseFromCorrectLink() throws Exception {
		HttpUtils.readURLData("https://www.gocypher.com/gocypher/status");
	}

	@Test(expected = NotFoundException.class)
	public void testResponseFromIncorrectink() throws Exception {
		HttpUtils.readURLData("http://local/bchRepair");
	}

	// TODO try to realise and test 404 exception for endpoints
	@Test
	public void testResponseFromIncorrectEndpoint() throws Exception {
		// sendAsyncPostRequest("/health_services/aa", "");
		// Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}

	public MockHttpResponse sendAsyncPostRequest(String path, String requestBody) throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest.get(path);
		MockHttpResponse response = new MockHttpResponse();
		return sendHttpRequest(request, response);
	}

	private MockHttpResponse sendHttpRequest(MockHttpRequest request, MockHttpResponse response) {
		dispatcher.invoke(request, response);
		return response;
	}
}