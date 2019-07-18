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

package com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.JsonRpcGeneric;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.IoUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;

/**
 * The type Zookeeper request processor.
 */
public class ZookeeperRequestProcessor {

	private ExecutorService executorService = Executors.newFixedThreadPool(5);

	private void invokeMethodWrapper(String classReference, Map<String, Object> params) throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Class<?> cls = Class.forName(classReference);
		Method methods = cls.getMethod("processRequest", Object.class);
		Object obj = cls.newInstance();

		executorService.submit(() -> {
			try {
				methods.invoke(obj, params);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void processRequest(String method, JsonRpcGeneric jsonRpcRequest, Properties properties) {
		Map<String, Object> params = (Map<String, Object>) jsonRpcRequest.getParams();
		params.put("properties", properties);
		try {
			invokeMethodWrapper(method, params);
		} catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException
				| ClassNotFoundException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	public void methodSelector(JsonRpcGeneric jsonRpcRequest) {
		Properties properties = IoUtils.propertiesWrapper(System.getProperty("listeners"));

		String method = properties.getProperty(jsonRpcRequest.getMethod());

		if (method != null) {
			processRequest(method, jsonRpcRequest, properties);
		} else {
			LoggerWrapper.addMessage(OpLevel.WARNING, "Received incorrect request");
		}
	}
}
