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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Http utils.
 */
public class HttpUtils {
	private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

	/**
	 * Read url via http string.
	 *
	 * @param urlString
	 *            the url string
	 * @return the string
	 */
	public static String readUrlViaHttp(String urlString) {
		String result = null;
		try {
			URL url = new URL(urlString);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

			result = readStreamAsString(conn.getInputStream(), "UTF-8");
		} catch (Exception e) {
			LOG.error("Error on accessing URL {} via HTTP", urlString, e);
		}
		return result;
	}

	private static String readStreamAsString(InputStream inputStream, String encoding) throws IOException {
		return readStream(inputStream).toString(encoding);
	}

	private static ByteArrayOutputStream readStream(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return baos;
	}

	/**
	 * Parse endpoints map.
	 *
	 * @param endpointStr
	 *            the endpoint str
	 * @return the map
	 */
	public static Map<String, String> parseEndpoints(String endpointStr) {
		Map<String, String> result = new HashMap<>();
		try {
			result = Arrays.stream(endpointStr.split(";")).map(s -> s.split("=")).collect(Collectors.toMap(a -> a[0], // key
					a -> a[1] // value
			));

		} catch (Exception e) {
			result.put(ClsConstants.KEY_ENDPOINT_PULL, endpointStr);
			result.put(ClsConstants.KEY_ENDPOINT_SUBSCRIBE, endpointStr);
			result.put(ClsConstants.KEY_ENDPOINT_PUSH, endpointStr);
		}
		return result;
	}

}
