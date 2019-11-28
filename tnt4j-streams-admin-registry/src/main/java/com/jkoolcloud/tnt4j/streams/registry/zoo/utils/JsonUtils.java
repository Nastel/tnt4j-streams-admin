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

package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;

/**
 * The type Static object mapper.
 */
public class JsonUtils {

	/**
	 * The constant mapper.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	private JsonUtils() {

	}

	public static String objectToString(Object json) {
		String jsonStr = null;
		try {
			jsonStr = JsonUtils.mapper.writeValueAsString(json);
		} catch (JsonProcessingException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
		return jsonStr;
	}

	public static <T> T jsonToObject(String filePath, Class<T> cls) {

		T data = null;
		try {
			data = JsonUtils.mapper.readValue(new File(filePath), cls);
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		return data;
	}

}
