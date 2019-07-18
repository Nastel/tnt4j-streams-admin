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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;
import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.ZookeeperAccessService;

/**
 * The type Service data.
 */
@Singleton
public class ServiceData {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceData.class);

	@Inject
	private ZookeeperAccessService zooKeeper;

	/**
	 * Instantiates a new Service data.
	 */
	public ServiceData() {
	}

	/**
	 * Service info parse to json string.
	 *
	 * @param serviceName
	 *            the service name
	 * @param allServiceCall
	 *            the all service call
	 * @return the string
	 * @throws IOException
	 *             the io exception
	 */
	// Reads the data from ones service and puts everything into JSON format
	public String serviceInfoParseToJSON(String serviceName, Boolean allServiceCall) throws IOException {

		String[] serviceDataNames = PropertyData.getProperty(ClsConstants.KEY_SERVICE_INFO_NAME)
				.split(ClsConstants.KEY_LIST_SEPARATOR);
		ObjectMapper objMapper = new ObjectMapper();
		ObjectWriter writer;
		boolean atLeastOneResponds = false;
		if (allServiceCall) {
			writer = objMapper.writer();
		} else {
			writer = objMapper.writer().withRootName(serviceName);
		}

		List<String> serviceLinks = createLinksToServiceData(serviceName);
		Map<String, Object> serviceDataFromJson;
		Map<String, Object> serviceData = new HashMap<>();
		String serviceDataName, serviceInfoData, jsonInString;

		for (String serviceLink : serviceLinks) {
			serviceInfoData = readURL(serviceLink);
			serviceDataName = checkServiceDataType(serviceLink, serviceDataNames);
			if (serviceInfoData != null && !serviceInfoData.isEmpty()
					&& serviceInfoData.compareTo(" java.net.ConnectException: Connection refused: connect") != 0) {
				LOG.info("Service data start_________________" + serviceInfoData);
				atLeastOneResponds = true;
				if (serviceInfoData.charAt(1) == '{' || serviceInfoData.charAt(0) == '{') {
					serviceDataFromJson = objMapper.readValue(serviceInfoData,
							new TypeReference<Map<String, Object>>() {
							});
					// serviceData.putAll(parseJsonDataIntoSimpleFormatZooKeeper(serviceDataFromJson)); //
					// parseJsonDataIntoSimpleFormat(serviceDataFromJson,
					// serviceData);
					// System.out.println("Service data _________________" + serviceData);
				} else {
					serviceData.put(serviceDataName, serviceInfoData);
					// System.out.println("Service data _________________" + serviceData);
				}
			}
		}

		jsonInString = writer.writeValueAsString(serviceData);

		if (atLeastOneResponds) {
			return jsonInString;
		} else {
			throw new NotFoundException("client connection failed no data got");
		}
	}

	/**
	 * Service info parse to json zoo keeper data string.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the string
	 * @throws IOException
	 *             the io exception
	 */
	public String serviceInfoParseToJSONZooKeeperData(String serviceName) throws IOException {

		LOG.info("Trying to get Zookeeper data from all command directly from ZooKeeper");
		// String[] serviceDataNames =
		// PropertyData.getProperty(ClsConstants.KEY_SERVICE_INFO_NAME).split(ClsConstants.KEY_LIST_SEPARATOR);
		ObjectMapper objMapper = new ObjectMapper();
		ObjectWriter writer;
		boolean atLeastOneResponds = false;

		writer = objMapper.writer().withRootName(serviceName);
		// List<String> serviceLinks = createLinksToServiceData(serviceName);
		Map<String, Object> serviceDataFromJson;
		Map<String, Object> serviceData = new HashMap<>();
		String jsonInString;

		LOG.info("Initializing components getting " + serviceName + " data");
		// serviceInfoData = readURL(serviceLink);
		// ZookeeperAccessService.init();
		// serviceData = zooKeeper.sendRequestAndWaitForResponse(serviceName, "getAllStats");

		LOG.info("Trying to get Zookeeper data from all command directly from ZooKeeper");
		Map<String, Object> tempMapAll = new HashMap<>();
		if (serviceData != null && !serviceData.isEmpty()) {
			for (Map.Entry<String, Object> entry : serviceData.entrySet()) {
				LOG.info("Service data from ALl ZooKeeper" + entry.getKey() + "  " + entry.getValue());
				atLeastOneResponds = true;
				if (entry.getValue().toString().charAt(0) == '{') {
					serviceDataFromJson = objMapper.readValue(entry.getValue().toString(),
							new TypeReference<Map<String, Object>>() {
							});
					// tempMapAll.putAll(parseJsonDataIntoSimpleFormatZooKeeper(serviceDataFromJson)); //
					// parseJsonDataIntoSimpleFormat(serviceDataFromJson,
					// serviceData);
					// System.out.println("Service data _________________" + serviceData);
				} else {
					tempMapAll.put(entry.getKey(), entry.getValue());
					// System.out.println("Service data _________________" + serviceData);
				}
			}
		}
		jsonInString = writer.writeValueAsString(tempMapAll);

		if (atLeastOneResponds) {
			return jsonInString;
		} else {
			throw new NotFoundException("client connection failed no data got");
		}
	}

	/**
	 * Service info parse to json from all data string.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the string
	 * @throws IOException
	 *             the io exception
	 */
	// Reads the data from ones service and puts everything into JSON format
	public String serviceInfoParseToJSONFromAllData(String serviceName) throws IOException {
		String serviceDataNames = PropertyData.getProperty(ClsConstants.KEY_SERVICE_ENDPOINT_FOR_ZOOKEEPER_ALL);
		// String[] serviceDataNames = .split(ClsConstants.KEY_LIST_SEPARATOR);
		ObjectMapper objMapper = new ObjectMapper();
		ObjectWriter writer;
		boolean atLeastOneResponds = false;
		writer = objMapper.writer();

		String builtServiceName = PropertyData.getProperty(serviceName + ClsConstants.KEY_SERVICE_LINK_BUILD_ENDING);
		// List<String> serviceLinks = createLinksToServiceData(serviceName);
		String serviceLink = PropertyData.getProperty(serviceName + ClsConstants.KEY_SERVICE_LINK_BUILD_ENDING)
				+ builtServiceName.replaceAll("\\s", "") + "/all";

		Map<String, Object> serviceDataFromJson;
		Map<String, Object> serviceData = new HashMap<>();
		String serviceDataName, serviceInfoData, jsonInString;

		// for (String serviceLink : serviceLinks) {
		serviceInfoData = readURL(serviceLink);
		Map<String, Object> tempMapMetrics = new HashMap<>();
		tempMapMetrics = objMapper.readValue(serviceInfoData, new TypeReference<Map<String, Object>>() {
		});

		// serviceDataName = checkServiceDataType(serviceLink, serviceDataNames);
		if (serviceInfoData != null && !serviceInfoData.isEmpty()
				&& serviceInfoData.compareTo(" java.net.ConnectException: Connection refused: connect") != 0) {
			for (Map.Entry<String, Object> entry : tempMapMetrics.entrySet()) {

				LOG.info("Service data start_________________" + serviceInfoData);
				atLeastOneResponds = true;
				if (entry.getValue().toString().charAt(1) == '{' || entry.getValue().toString().charAt(0) == '{') {
					serviceDataFromJson = objMapper.readValue(serviceInfoData,
							new TypeReference<Map<String, Object>>() {
							});
					// serviceData.putAll(parseJsonDataIntoSimpleFormatZooKeeper(serviceDataFromJson)); //
					// parseJsonDataIntoSimpleFormat(serviceDataFromJson,
					// serviceData);
					// System.out.println("Service data _________________" + serviceData);
				} else {
					serviceData.put(entry.getKey(), entry.getValue());
					// System.out.println("Service data _________________" + serviceData);
				}
			}
		}
		// }

		jsonInString = writer.writeValueAsString(serviceData);

		if (atLeastOneResponds) {
			return jsonInString;
		} else {
			throw new NotFoundException("client connection failed no data got");
		}
	}

	/**
	 * Read all data string.
	 *
	 * @return the string
	 * @throws IOException
	 *             the io exception
	 */
	// Reads all the data from the services and returns in JSON
	public String readAllData() throws IOException {

		ObjectMapper objMapper = new ObjectMapper();
		ObjectWriter writer = objMapper.writer();
		Map<String, Object> serviceDataFromJson;

		String[] serviceName = PropertyData.getProperty(ClsConstants.KEY_SERVICE_ENDPOINT_LIST)
				.split(ClsConstants.KEY_LIST_SEPARATOR);
		Map<String, Object> serviceData = new HashMap<>();

		for (String service : serviceName) {
			serviceDataFromJson = objMapper.readValue(serviceInfoParseToJSON(service.replaceAll("\\s", ""), true),
					new TypeReference<Map<String, Object>>() {
					});
			serviceData.put(service.replaceAll("\\s", ""), serviceDataFromJson);
		}
		return writer.writeValueAsString(serviceData);
	}

	private Map<String, Object> parseJsonDataIntoSimpleFormat(Map<String, Object> serviceDataFromJson,
			Map<String, Object> serviceData) throws IOException {

		HashMap<String, Object> serviceDataFromJson1, serviceDataFromJson2;
		ObjectMapper objMapper = new ObjectMapper();
		ObjectWriter writer = objMapper.writer();
		String jsonInString, jsonInString1;
		for (HashMap.Entry<String, Object> entry : serviceDataFromJson.entrySet()) {
			if (entry.getValue() instanceof Map) {
				jsonInString = writer.writeValueAsString(entry.getValue());
				serviceDataFromJson1 = objMapper.readValue(jsonInString, new TypeReference<Map<String, Object>>() {
				});
				for (HashMap.Entry<String, Object> entry1 : serviceDataFromJson1.entrySet()) {
					if (((Map) entry.getValue()).size() == 1) {
						serviceData.put(entry.getKey().replaceAll("\\s", ""), entry1.getValue());
					} else {
						if (entry1.getValue() instanceof Map) {
							jsonInString1 = writer.writeValueAsString(entry1.getValue());
							serviceDataFromJson2 = objMapper.readValue(jsonInString1,
									new TypeReference<Map<String, Object>>() {
									});
							for (HashMap.Entry<String, Object> entry2 : serviceDataFromJson2.entrySet()) {
								if (((Map) entry1.getValue()).size() == 1) {
									serviceData.put(entry1.getKey().replaceAll("\\s", ""), entry2.getValue());
								} else {
									serviceData.put(entry1.getKey().replaceAll("\\s", ""), entry1.getValue());
								}
							}
						}
					}
				}
			} else {
				serviceData.put(entry.getKey(), entry.getValue());
				// LOG.info("The item is not an object"+ entry.getKey() +"_ _"+ entry.getValue());
			}
		}
		return serviceData;
	}

	/**
	 * Parse json data into simple format zoo keeper map.
	 *
	 * @param serviceDataFromJson
	 *            the service data from json
	 * @return the map
	 * @throws IOException
	 *             the io exception
	 */
	public static Map<String, Object> parseJsonDataIntoSimpleFormatZooKeeper(Map<String, Object> serviceDataFromJson,
			String serviceName) {
		Map<String, Object> serviceData = new HashMap<>();
		try {
			HashMap<String, Object> serviceDataFromJson1, serviceDataFromJson2, serviceDataFromJson3;
			ObjectMapper objMapper = new ObjectMapper();
			ObjectWriter writer = objMapper.writer();
			String jsonInString, jsonInString1, jsonInString2;
			for (HashMap.Entry<String, Object> entry : serviceDataFromJson.entrySet()) {
				if (entry.getValue() instanceof Map) {
					jsonInString = writer.writeValueAsString(entry.getValue());
					jsonInString = jsonInString.replace(serviceName + " ", "");
					serviceDataFromJson1 = objMapper.readValue(jsonInString, new TypeReference<Map<String, Object>>() {
					});
					// Getting the 14 values
					for (HashMap.Entry<String, Object> entry1 : serviceDataFromJson1.entrySet()) {
						jsonInString1 = writer.writeValueAsString(entry1.getValue());
						serviceDataFromJson2 = objMapper.readValue(jsonInString1,
								new TypeReference<Map<String, Object>>() {
								});
						if (entry1.getValue() instanceof Map) {
							for (HashMap.Entry<String, Object> entry2 : serviceDataFromJson2.entrySet()) {
								if (entry2.getValue() instanceof Map) {
									jsonInString2 = writer.writeValueAsString(entry2.getValue());
									serviceDataFromJson3 = objMapper.readValue(jsonInString2,
											new TypeReference<Map<String, Object>>() {
											});
									for (HashMap.Entry<String, Object> entry3 : serviceDataFromJson3.entrySet()) {
										if (!(entry3.getValue() instanceof Map)) {
											serviceData.put(entry2.getKey() + " " + entry1.getKey(), entry2.getValue());
										}
									}
								} else {
									if (serviceDataFromJson2.size() > 1) {
										serviceData.put(entry1.getKey(), entry1.getValue());
									} else {
										serviceData.put(entry1.getKey(), entry2.getValue());
									}
								}
							}
						} else {
							serviceData.put(entry.getKey().replaceAll("\\s", ""), entry1.getValue());
						}
					}
				} else {
					serviceData.put(entry.getKey(), entry.getValue());
				}
			}

		} catch (Exception e) {
			LOG.error("Problem with ZooKeeper metrics formatting ", e);
		}
		return serviceData;
	}

	// Takes the information from the serviceInfoLinkCfg.properties and puts it together into a list of service links
	private List<String> createLinksToServiceData(String serviceName) throws IOException {
		List<String> serviceLinks = new ArrayList<>();
		String[] serviceInfoNames = PropertyData.getProperty(ClsConstants.KEY_SERVICE_INFO_NAME)
				.split(ClsConstants.KEY_LIST_SEPARATOR);
		String builtServiceName = PropertyData.getProperty(serviceName + ClsConstants.KEY_SERVICE_LINK_BUILD_ENDING);

		for (String serviceInfo : serviceInfoNames) {
			serviceLinks.add(builtServiceName.replaceAll("\\s", "") + "/" + serviceInfo.replaceAll("\\s+", ""));

		}
		return serviceLinks;
	}

	// Finds which metric from the service is being read in order to creating nice looking JSON
	private String checkServiceDataType(String serviceLink, String[] serviceDataNames) {
		String dataType = "";
		for (String serviceDataName : serviceDataNames) {
			if (serviceLink.contains(serviceDataName.replaceAll("\\s", ""))) {
				dataType = serviceDataName;// +"s";
			}
		}
		return dataType.replaceAll("\\s", "");
	}

	/**
	 * Read url string.
	 *
	 * @param serviceLink
	 *            the service link
	 * @return the string
	 */
	// Reads http or https URL returns all the data from the page
	public static String readURL(String serviceLink) {
		LOG.error(serviceLink);
		String line;
		String response = "";
		try {
			URL url = new URL(serviceLink);
			// open the url stream, wrap it an a few "readers"
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

			while ((line = reader.readLine()) != null) {
				response += line;
			}

			reader.close();

		} catch (Exception e) {
			LOG.error("The link provided " + serviceLink + " was wrong or can not be accessed at the moment");
			// throw new NotFoundException("client connection to " + serviceLink + " fail: no connection");
		}
		return response;
	}
}
