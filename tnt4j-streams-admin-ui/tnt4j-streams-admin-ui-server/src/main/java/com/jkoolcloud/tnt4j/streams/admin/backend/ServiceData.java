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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.HttpUtils;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;

/**
 * The type Service dataReading.
 */
@Singleton
public class ServiceData {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceData.class);

	/**
	 * Instantiates a new Service dataReading.
	 */
	public ServiceData() {
	}

	/**
	 * Parse json dataReading into simple format zoo keeper map.
	 *
	 * @param serviceDataFromJson
	 *            the service dataReading from json
	 * @return the map
	 * @throws IOException
	 *             the io exception
	 */
	public static Map<String, Object> parseJsonDataIntoSimpleFormatZooKeeper(Map<String, Object> serviceDataFromJson,
			String serviceName) {
		Map<String, Object> serviceData = new HashMap<>();
		try {
			Map<String, Object> serviceDataFromJson1, serviceDataFromJson2, serviceDataFromJson3;
			ObjectMapper objMapper = new ObjectMapper();
			ObjectWriter writer = objMapper.writer();
			String jsonInString, jsonInString1, jsonInString2;
			for (HashMap.Entry<String, Object> entry : serviceDataFromJson.entrySet()) {
				if (entry.getValue() instanceof Map && !entry.getKey().equals("config")) {
					jsonInString = writer.writeValueAsString(entry.getValue());
					jsonInString = jsonInString.replace(serviceName + " ", "");
					serviceDataFromJson1 = objMapper.readValue(jsonInString, HashMap.class);
					// Getting the 14 values
					for (HashMap.Entry<String, Object> entry1 : serviceDataFromJson1.entrySet()) {
						jsonInString1 = writer.writeValueAsString(entry1.getValue());
						serviceDataFromJson2 = objMapper.readValue(jsonInString1, HashMap.class);
						if (entry1.getValue() instanceof Map) {
							for (HashMap.Entry<String, Object> entry2 : serviceDataFromJson2.entrySet()) {
								if (entry2.getValue() instanceof Map) {
									jsonInString2 = writer.writeValueAsString(entry2.getValue());
									serviceDataFromJson3 = objMapper.readValue(jsonInString2, HashMap.class);
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
	// Reads the dataReading from ones service and puts everything into JSON format
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
			serviceInfoData = HttpUtils.readURLData(serviceLink);
			serviceDataName = checkServiceDataType(serviceLink, serviceDataNames);
			if (serviceInfoData != null && !serviceInfoData.isEmpty()
					&& serviceInfoData.compareTo(" java.net.ConnectException: Connection refused: connect") != 0) {
				LOG.info("Service dataReading start_________________" + serviceInfoData);
				atLeastOneResponds = true;
				if (serviceInfoData.charAt(1) == '{' || serviceInfoData.charAt(0) == '{') {
					serviceDataFromJson = objMapper.readValue(serviceInfoData, HashMap.class);
					// serviceData.putAll(parseJsonDataIntoSimpleFormatZooKeeper(serviceDataFromJson)); //
					// parseJsonDataIntoSimpleFormat(serviceDataFromJson,
					// serviceData);
					// System.out.println("Service dataReading _________________" + serviceData);
				} else {
					serviceData.put(serviceDataName, serviceInfoData);
					// System.out.println("Service dataReading _________________" + serviceData);
				}
			}
		}

		jsonInString = writer.writeValueAsString(serviceData);

		if (atLeastOneResponds) {
			return jsonInString;
		} else {
			throw new NotFoundException("client connection failed no dataReading got");
		}
	}

	/**
	 * Service info parse to json zoo keeper dataReading string.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the string
	 * @throws IOException
	 *             the io exception
	 */
	public String serviceInfoParseToJSONZooKeeperData(String serviceName) throws IOException {

		LOG.info("Trying to get Zookeeper dataReading from all command directly from ZooKeeper");
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

		LOG.info("Initializing components getting " + serviceName + " dataReading");
		// serviceInfoData = HttpUtils.readURLData(serviceLink);
		// ZookeeperAccessService.init();
		// serviceData = zooKeeper.sendRequestAndWaitForResponse(serviceName, "getAllStats");

		LOG.info("Trying to get Zookeeper dataReading from all command directly from ZooKeeper");
		Map<String, Object> tempMapAll = new HashMap<>();
		if (serviceData != null && !serviceData.isEmpty()) {
			for (Map.Entry<String, Object> entry : serviceData.entrySet()) {
				LOG.info("Service dataReading from ALl ZooKeeper" + entry.getKey() + "  " + entry.getValue());
				atLeastOneResponds = true;
				if (entry.getValue().toString().charAt(0) == '{') {
					serviceDataFromJson = objMapper.readValue(entry.getValue().toString(), HashMap.class);
					// tempMapAll.putAll(parseJsonDataIntoSimpleFormatZooKeeper(serviceDataFromJson)); //
					// parseJsonDataIntoSimpleFormat(serviceDataFromJson,
					// serviceData);
					// System.out.println("Service dataReading _________________" + serviceData);
				} else {
					tempMapAll.put(entry.getKey(), entry.getValue());
					// System.out.println("Service dataReading _________________" + serviceData);
				}
			}
		}
		jsonInString = writer.writeValueAsString(tempMapAll);

		if (atLeastOneResponds) {
			return jsonInString;
		} else {
			throw new NotFoundException("client connection failed no dataReading got");
		}
	}

	/**
	 * Service info parse to json from all dataReading string.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the string
	 * @throws IOException
	 *             the io exception
	 */
	// Reads the dataReading from ones service and puts everything into JSON format
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
		serviceInfoData = HttpUtils.readURLData(serviceLink);
		Map<String, Object> tempMapMetrics = new HashMap<>();
		tempMapMetrics = objMapper.readValue(serviceInfoData, HashMap.class);

		// serviceDataName = checkServiceDataType(serviceLink, serviceDataNames);
		if (serviceInfoData != null && !serviceInfoData.isEmpty()
				&& serviceInfoData.compareTo(" java.net.ConnectException: Connection refused: connect") != 0) {
			for (Map.Entry<String, Object> entry : tempMapMetrics.entrySet()) {

				LOG.info("Service dataReading start_________________" + serviceInfoData);
				atLeastOneResponds = true;
				if (entry.getValue().toString().charAt(1) == '{' || entry.getValue().toString().charAt(0) == '{') {
					serviceDataFromJson = objMapper.readValue(serviceInfoData, HashMap.class);
					// serviceData.putAll(parseJsonDataIntoSimpleFormatZooKeeper(serviceDataFromJson)); //
					// parseJsonDataIntoSimpleFormat(serviceDataFromJson,
					// serviceData);
					// System.out.println("Service dataReading _________________" + serviceData);
				} else {
					serviceData.put(entry.getKey(), entry.getValue());
					// System.out.println("Service dataReading _________________" + serviceData);
				}
			}
		}
		// }

		jsonInString = writer.writeValueAsString(serviceData);

		if (atLeastOneResponds) {
			return jsonInString;
		} else {
			throw new NotFoundException("client connection failed no dataReading got");
		}
	}

	/**
	 * Read all dataReading string.
	 *
	 * @return the string
	 * @throws IOException
	 *             the io exception
	 */
	// Reads all the dataReading from the services and returns in JSON
	public String readAllData() throws IOException {

		ObjectMapper objMapper = new ObjectMapper();
		ObjectWriter writer = objMapper.writer();
		Map<String, Object> serviceDataFromJson;

		String[] serviceName = PropertyData.getProperty(ClsConstants.KEY_SERVICE_ENDPOINT_LIST)
				.split(ClsConstants.KEY_LIST_SEPARATOR);
		Map<String, Object> serviceData = new HashMap<>();

		for (String service : serviceName) {
			serviceDataFromJson = objMapper.readValue(serviceInfoParseToJSON(service.replaceAll("\\s", ""), true),
					HashMap.class);
			serviceData.put(service.replaceAll("\\s", ""), serviceDataFromJson);
		}
		return writer.writeValueAsString(serviceData);
	}

	private Map<String, Object> parseJsonDataIntoSimpleFormat(Map<String, Object> serviceDataFromJson,
			Map<String, Object> serviceData) throws IOException {

		Map<String, Object> serviceDataFromJson1;
		Map<String, Object> serviceDataFromJson2;
		ObjectMapper objMapper = new ObjectMapper();
		ObjectWriter writer = objMapper.writer();
		String jsonInString, jsonInString1;
		for (HashMap.Entry<String, Object> entry : serviceDataFromJson.entrySet()) {
			if (entry.getValue() instanceof Map) {
				jsonInString = writer.writeValueAsString(entry.getValue());
				serviceDataFromJson1 = objMapper.readValue(jsonInString, HashMap.class);
				for (HashMap.Entry<String, Object> entry1 : serviceDataFromJson1.entrySet()) {
					if (((Map) entry.getValue()).size() == 1) {
						serviceData.put(entry.getKey().replaceAll("\\s", ""), entry1.getValue());
					} else {
						if (entry1.getValue() instanceof Map) {
							jsonInString1 = writer.writeValueAsString(entry1.getValue());
							serviceDataFromJson2 = objMapper.readValue(jsonInString1, HashMap.class);
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

}
