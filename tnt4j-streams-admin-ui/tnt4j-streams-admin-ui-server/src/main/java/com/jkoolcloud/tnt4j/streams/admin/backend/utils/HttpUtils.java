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

import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.NotFoundException;

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
	 * Read the data from provided URL address without address status information
	 * @param serviceLink
	 * 			The provided URL address, to read data from
	 * @return
	 */
	public static String readURLData(String serviceLink) {
		String response = "";
		LOG.info("Trying to read dataReading from {}", serviceLink);
		try {
			URL url = new URL(serviceLink);
			URLConnection con = url.openConnection();
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			InputStream in = con.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while (true) {
				String line;
				if ((line = reader.readLine()) == null) {
					reader.close();
					break; }
				line = line.replaceFirst("^,", "");
				response = response + line;
				response = response + "\n";
			}
		} catch (SocketTimeoutException e) {
			LOG.error("The provided endpoint: "+ serviceLink + " took to long to respond ( > 5s )");
			return "The provided endpoint took to long to respond ( > 5s )";
		} catch (MalformedURLException e) {
			LOG.error("The provided endpoint: "+ serviceLink + " returned an empty string MalformedURLException");
			return "MalformedURLException";
		} catch (Exception e) {
			LOG.error("The link provided " + serviceLink + " was wrong or can not be accessed at the moment");
			throw new NotFoundException("client connection to " + serviceLink + " fail: no connection"+ e);
		}

		return response;
	}

	/**
	 * Read the data from provided URL address without address status information
	 * @param serviceLink
	 * 			The provided URL address, to read data from
	 * @return
	 */
	public static String readURLDataWithToken(String serviceLink, String tempToken) {
		String response = "";
		LOG.info("Trying to read dataReading from {}", serviceLink);
		try {
			URL url = new URL(serviceLink);
			URLConnection con = url.openConnection();
			//con.set
			con.setRequestProperty("Authorization", tempToken);
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			InputStream in = con.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while (true) {
				String line;
				if ((line = reader.readLine()) == null) {
					reader.close();
					break; }
				line = line.replaceFirst("^,", "");
				response = response + line;
				response = response + "\n";
			}
		} catch (SocketTimeoutException e) {
			LOG.error("The provided endpoint: "+ serviceLink + " took to long to respond ( > 5s )");
			return "The provided endpoint took to long to respond ( > 5s )";
		} catch (MalformedURLException e) {
			LOG.error("The provided endpoint: "+ serviceLink + " returned an empty string MalformedURLException");
			return "MalformedURLException";
		} catch (Exception e) {
			LOG.error("The link provided " + serviceLink + " was wrong or can not be accessed at the moment");
			throw new NotFoundException("client connection to " + serviceLink + " fail: no connection"+ e);
		}

		return response;
	}


//	public static InputStream readUrlAsInputStreamHttps(String httpsUrl, boolean ignoreHostnameVerifier, String tempToken) throws IOException {
//		URL url = new URL(httpsUrl);
//		LOG.info("TOKEN : {}", tempToken);
//			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
//			con.setRequestProperty("Authorization", tempToken);
//			con.setConnectTimeout(5000);
//			con.setReadTimeout(5000);
//			if (ignoreHostnameVerifier) {
//				con.setHostnameVerifier((s, sslSession) -> true);
//			}
//
//
//		return con.getInputStream();
//	}

	/**
	 * Method to read data with token authorization and SSL authentication from ZooKeeper nodes REST
	 * @param httpsUrl
	 * @param ignoreHostnameVerifier
	 * @param tempToken
	 * @return
	 * @throws IOException
	 */
	public static String readUrlAsStringWithToken(String httpsUrl, boolean ignoreHostnameVerifier, String tempToken) throws IOException {
		URL url = new URL(null, httpsUrl, new sun.net.www.protocol.https.Handler());
		StringBuilder responseBuf = new StringBuilder();
		try {
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setRequestProperty("Authorization", tempToken);
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			if (ignoreHostnameVerifier) {
				con.setHostnameVerifier((s, sslSession) -> true);
			}
			LOG.info("http URL {}", httpsUrl);
			LOG.info("Temp Token {}", tempToken);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				responseBuf.append(line);
			}

			bufferedReader.close();

		} catch (SocketTimeoutException e) {
			LOG.error("The provided endpoint: "+ httpsUrl + " took to long to respond ( > 5s )");
			return "The provided endpoint took to long to respond ( > 5s )";
		} catch (MalformedURLException e) {
			LOG.error("The provided endpoint: "+ httpsUrl + " returned an empty string MalformedURLException");
			return "MalformedURLException";
		} catch (Exception e) {
			LOG.error("The link provided " + httpsUrl + " was wrong or can not be accessed at the moment");
			throw new NotFoundException("client connection to " + httpsUrl + " fail: no connection"+ e);
		}

		return responseBuf.toString();
	}

}
