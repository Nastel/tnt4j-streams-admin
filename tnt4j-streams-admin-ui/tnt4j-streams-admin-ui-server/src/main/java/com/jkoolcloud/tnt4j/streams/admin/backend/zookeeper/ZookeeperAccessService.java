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

package com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper;

import java.io.IOException;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.naming.AuthenticationException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jkoolcloud.tnt4j.streams.admin.backend.ServiceData;
import com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth.LoginCache;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.HttpUtils;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.RequestPath;
import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.utils.JsonRpc;

/**
 * The type Zookeeper access service for getting ZooKeeper dataReading to return to API.
 */
@Singleton
public class ZookeeperAccessService {
	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperAccessService.class);

	private static String AUTH_NODE_PATH_ACTION_RIGHTS, AUTH_NODE_PATH_READ_RIGHTS, SERVICES_REGISTRY_START_NODE,
			SERVICES_REGISTRY_START_PARENT, ACTIVE_STREAMS_REGISTRY_NODE, credentials, TOKEN_TYPE;

	private static CuratorFramework client;
	private static int AGENT_DEPTH;

	private static ZooKeeperConnectionManager zooManager = new ZooKeeperConnectionManager();

	/**
	 * Init ZooKeeper connection.
	 */
	@PostConstruct
	public static void init() {
		try {
			SERVICES_REGISTRY_START_NODE = PropertyData.getProperty("serviceRegistryStartNode");
			SERVICES_REGISTRY_START_PARENT = PropertyData.getProperty("serviceRegistryStartNodeParent");
			ACTIVE_STREAMS_REGISTRY_NODE = PropertyData.getProperty("activeStreamRegistry");
			AUTH_NODE_PATH_ACTION_RIGHTS = PropertyData.getProperty("authorizationTokenAction");
			AUTH_NODE_PATH_READ_RIGHTS = PropertyData.getProperty("authorizationTokenRead");
			AGENT_DEPTH = Integer.parseInt(PropertyData.getProperty("depthToAgentNode"));
			TOKEN_TYPE = PropertyData.getProperty("tokenType");
			String sslConfigFilePath = PropertyData.getProperty("SslConfigFilePath");
			String sslPass = PropertyData.getProperty("SslPass");
			if (!sslConfigFilePath.isEmpty() && !sslPass.isEmpty()) {
				System.setProperty("javax.net.ssl.trustStore", sslConfigFilePath);
				System.setProperty("javax.net.ssl.trustStorePassword", sslPass);
			}
			// if (credentials == null || credentials.isEmpty()) {
			// destroy();
			// LOG.info("No credentials no connection to ZooKeeper ZOOKEEPER_URL");
			// }
			if (!client.getState().toString().equals("STARTED")) {
				LOG.info("CLIENT NOT YET STARTED: ");
				client.start();
			} else {
				LOG.info("CLIENT STARTED: ");
			}
		} catch (Exception e) {
			LOG.error("Error on Zookeeper connection start", e);
		}
	}

	public static CuratorFramework getConnection() throws AuthenticationException {
		return zooManager.getClientConnection();
	}

	public static CuratorFramework getConnectionAdmin() {
		String ZOOKEEPER_URL;
		CuratorFramework admin = null;
		try {
			String credentialsAdmin = PropertyData.getProperty("UserManagerUsername") + ":"
					+ PropertyData.getProperty("UserManagerPassword");
			ZOOKEEPER_URL = PropertyData.getProperty("ZooKeeperAddress");
			LOG.info("Credentials for admin connection: {}", credentials);

			CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString(ZOOKEEPER_URL)
					.retryPolicy(new ExponentialBackoffRetry(1000, 3))
					.authorization("digest", credentialsAdmin.getBytes());
			admin = builder.build();

			LOG.info("Connection start admin");
			admin.start();
			init();
		} catch (IOException e) {
			LOG.info("problem on reading property data");
		}
		return admin;
	}

	public static void stopConnectionCurator(CuratorFramework adminConn) {
		LOG.error("stopConnectionCurator called for: {}", adminConn.getData());
		if (adminConn != null) {
			try {
				adminConn.close();
			} catch (Exception e) {
				LOG.error("Error on zookeeper disconnect");
			}
		}
	}

	private static String removeLastChar(String str) {
		return str.substring(0, str.length() - 1);
	}

	/**
	 * Return a JSON object no matter of the starting object format.
	 *
	 * @param data
	 * @return
	 */
	public static HashMap<String, Object> getResponseInJson(String data) {
		HashMap<String, Object> dataMap = new HashMap<>();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if (data.charAt(0) == '[') {
				dataMap.put("data", data);
			} else if (JsonRpc.isJSONValid(data)) {
				dataMap = objectMapper.readValue(data, HashMap.class);
			} else {
				dataMap.put("dataReading", data);
			}
		} catch (Exception e) {
			LOG.error("Error on putting data into a map object", e);
		}
		return dataMap;
	}

	/**
	 * Get the map dataReading ant format it using the method prom service dataReading
	 *
	 * @param dataMap
	 *            The dataReading from ZooKeeper node inside Map
	 * @return
	 */
	private static Map<String, Object> getMetricsWithFormattingStreams(Object dataMap, String serviceName) {
		Map<String, Object> tempMap = null;
		HashMap<String, Object> tempData = new HashMap<>();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String json = objectMapper.writeValueAsString(dataMap);
			dataMap = objectMapper.readValue(json, HashMap.class);
			tempData.put(serviceName, dataMap);
			tempMap = ServiceData.parseJsonDataIntoSimpleFormatZooKeeper(tempData, serviceName);
		} catch (Exception e) {
			LOG.error("Problem while trying to parse metrics dataReading", e);
		}
		return tempMap;
	}

	/**
	 * Get the map dataReading ant format it using the method prom service dataReading
	 *
	 * @param dataMap
	 *            The dataReading from ZooKeeper node inside Map
	 * @return
	 */
	private static Map<String, Object> getMetricsWithFormatting(HashMap<String, Object> dataMap, String serviceName) {
		Map<String, Object> tempMap = null;
		try {
			tempMap = ServiceData.parseJsonDataIntoSimpleFormatZooKeeper(dataMap, serviceName);
			// LOG.info("Formatted metrics dataReading {}", tempMap);

		} catch (Exception e) {
			LOG.error("Problem while trying to parse metrics dataReading", e);
		}
		return tempMap;
	}

	/**
	 * Check if the dataReading call is for metrics dataReading
	 *
	 * @param dataMap
	 *            The dataReading from ZooKeeper node inside Map
	 * @return
	 */
	private static boolean checkIfMetricsData(HashMap<String, Object> dataMap) {
		HashMap<String, Object> configData = (HashMap<String, Object>) dataMap.get("config");
		if (configData != null) {
			return configData.get("componentLoad").toString().equals("metrics")
					|| configData.get("componentLoad").toString().equals("service");
		} else {
			return false;
		}
	}

	private static boolean checkIfNeededURL(String pathToData, String neededName) {
		try {
			String idStr = pathToData.substring(pathToData.lastIndexOf('/') + 1);
			if (idStr.equals(neededName)) {
				return true;
			}
		} catch (Exception e) {
			LOG.error("There was a problem while checking if the call is from {} node", pathToData);
		}
		return false;
	}

	/**
	 * Checks if the provided path is a request to download a file.
	 *
	 * @param pathToData
	 * @param neededName
	 * @return
	 */
	private static boolean checkIfNeededURLControls(String pathToData, String neededName) {
		try {
			String[] arrayUrl = pathToData.split("/");
			if (arrayUrl[arrayUrl.length - 1].contains(neededName)) {
				return true;
			}
		} catch (Exception e) {
			LOG.error("There was a problem while checking if the call is from {} node", pathToData);
		}
		return false;
	}

	/**
	 * Checks if the provided path is a request to download a file.
	 *
	 * @param pathToData
	 * @param neededName
	 * @return
	 */
	private static boolean checkIfNeededURLDownload(String pathToData, String neededName) {
		try {
			String[] arrayUrl = pathToData.split("/");
			if (arrayUrl[arrayUrl.length - 2].contains(neededName)) {
				return true;
			}
		} catch (Exception e) {
			LOG.error("There was a problem while checking if the call is from {} node", pathToData);
		}
		return false;
	}

	/**
	 * Get service name from inside the metrics information
	 *
	 * @param dataMap
	 *            The dataReading from ZooKeeper node inside Map
	 * @return
	 */
	private static String getStreamName(HashMap<String, Object> dataMap) {
		HashMap<String, Object> serviceData = (HashMap<String, Object>) dataMap.get("data");
		String serviceName;
		String data = serviceData.toString().substring(1);
		int index = data.indexOf(' ');
		if (index > -1) {
			serviceName = data.substring(0, index);
		} else {
			serviceName = data;
		}
		serviceData.get(serviceName);
		return serviceName;
	}

	/**
	 * Destroy ZooKeeper connection.
	 */
	@PreDestroy
	public static void destroy() {
		LOG.info("Stopping connection to Zookeeper");
		if (client != null) {
			try {
				client.close();
			} catch (Exception e) {
				LOG.error("Error on zookeeper disconnect");
			}
		}
		LOG.info("Connection to Zookeeper closed.");
	}

	public static void connectToZooKeeper(String credentialsSent) {
		String ZOOKEEPER_URL = null;
		try {
			client = zooManager.addClientConnection(credentialsSent);
			init();
			// ZOOKEEPER_URL = PropertyData.getProperty("ZooKeeperAddress");
			// LOG.info("Connecting to Zookeeper at {}" ZOOKEEPER_URL);
			// builder = CuratorFrameworkFactory.builder().connectString(ZOOKEEPER_URL)
			// .retryPolicy(new ExponentialBackoffRetry(1000, 3))
			// .authorization("digest", credentialsSent.getBytes());
			// client = builder.build();
			credentials = credentialsSent;

			// } catch (IOException e) {
			// LOG.error("Problem on reading properties file information");
		} catch (Exception e) {
			LOG.error("An unexpected error occurred on connection to ZooKeeper with credentials");
		}
	}

	/**
	 * Read data from the provided node
	 *
	 * @param nodePath
	 * @return
	 */
	public static String readNode(String nodePath) {
		String responseData = "";
		try {
			byte[] nodeLinkBytes = client.getData().watched().forPath(nodePath);
			responseData = new String(nodeLinkBytes);
			// LOG.info("Node data: {}", responseData);
		} catch (Exception e) {
			LOG.error("Error on query for node information read node {}", nodePath);
		}
		return responseData;
	}

	/**
	 * Use to check if the credentials validation was successful and if the user has admin rights
	 */
	public static boolean checkIfConnected() {
		LoginCache cache = new LoginCache();
		boolean userConnected = false, userAdmin;
		if (client.getState() == CuratorFrameworkState.STARTED) {
			Collection<String> clusterNodes = CuratorUtils.nodeChildrenList(SERVICES_REGISTRY_START_NODE, client);
			for (String cluster : clusterNodes) {
				String tempClusterNode = SERVICES_REGISTRY_START_NODE + "/" + cluster;
				try {
					String NodeData = readNode(SERVICES_REGISTRY_START_NODE + "/" + cluster);
					if (NodeData.isEmpty()) {
					} else {
						if (!userConnected) {
							userConnected = true;
							String tokenNeeded = cache.generateTokenForUser();
							LOG.trace("Check if connected and since not create a token for connection");
							zooManager.setConnectionToken(tokenNeeded);
							zooManager.setClientConnection(client);
						}
						userAdmin = CuratorUtils.checkIfUserIsAdmin(client, tempClusterNode, credentials);
						if (userAdmin) {
							cache.setIsUserAdmin(true);
						}
					}
				} catch (Exception e) {
					LOG.error("Problem on checking user connection or getting the users list for admin");
				}
			}
		}
		return userConnected;
	}

	/**
	 * Getting node tree parameters set
	 *
	 * @return The map of all nodes together with their parent and lvl
	 */
	public static Map<String, String> getTreeNodes(String userToken) throws AuthenticationException {
		setTokenAndGetConn(userToken);
		Map<String, String> myNodeMap = new HashMap<>();
		String parentNode = "BaseNode";
		int nodeLevel = 0;
		Map<String, String> nodeMap = getZooKeeperTreeNodes(SERVICES_REGISTRY_START_NODE, myNodeMap, parentNode,
				nodeLevel);
		// LOG.info("ZooKeeper Node Tree Map ={}", nodeMap.size());
		return nodeMap;
	}

	/**
	 * Method that uses recursion to get all the needed nodes from ZooKeeper
	 *
	 * @param path
	 *            The starting ZooKeeper node
	 * @param myNodeMap
	 *            Node map
	 * @param parentNode
	 *            The node for saving the parent node value with current node "initial : BaseNode"
	 * @return The map of all nodes together with their parent nodes
	 */
	private static Map<String, String> getZooKeeperTreeNodes(String path, Map<String, String> myNodeMap,
			String parentNode, int nodeLevel) {
		try {
			Collection<String> serviceNames;
			String nodeName = getAddressEnding(path);
			Stat statResponse = client.checkExists().forPath(path);
			if (statResponse == null) {
				LOG.info("No call path exists: {} Check the configuration file {}", path);
			} else if (nodeName.charAt(0) == '_') {
				// LOG.info("The node {} does not need to be shown in tree view {}", path);
			} else {
				Map<String, Integer> tempNode = new HashMap<>();
				// tempNode.put(path, nodeLevel);
				// String json = new ObjectMapper().writeValueAsString(tempNode);
				String NodeData = readNode(path);
				if (!NodeData.isEmpty()) {
					myNodeMap.put(path, parentNode);
				}
				parentNode = path;
				nodeLevel++;
				serviceNames = CuratorUtils.nodeChildrenList(path, client);
				for (String serviceName : serviceNames) {
					try {
						String pathToNextTreeLvl = path + "/" + serviceName;
						getZooKeeperTreeNodes(pathToNextTreeLvl, myNodeMap, parentNode, nodeLevel);
					} catch (Exception e1) {
						LOG.error("Error on tree branch creation", e1);
						destroy();
					}
				}
			}
		} catch (Exception e) {
			LOG.error("No access to tree nodes");
		}
		return myNodeMap;
	}

	/**
	 * Method that finishes building the path to ZooKeeper node, reads the address from node, and returns the data from
	 * the address inside a map object.
	 *
	 * @param pathToData
	 *            The path got on request from front-end or API user to get the dataReading from ZooKeeper node
	 * @return
	 */
	public static HashMap<String, Object> getServiceNodeInfoFromLinkForReplay(String pathToData, String userToken)
			throws AuthenticationException {
		setTokenAndGetConn(userToken);
		String responseLink, responseData, pathToNode, blocksToReplay;
		HashMap<String, Object> dataMap = new HashMap<>();
		try {
			String[] nodeParts = pathToData.split("/");
			String requestToken = getTheTokenFromZooKeeper(pathToData, AUTH_NODE_PATH_ACTION_RIGHTS);
			requestToken = TOKEN_TYPE + " " + requestToken;
			pathToNode = RequestPath.getPathOfSpecifiedLength(pathToData, AGENT_DEPTH);
			pathToNode = prepareReplayLink(pathToNode);
			LOG.info("pathToNode For replay {}", pathToNode);
			blocksToReplay = getAddressEnding(pathToData);
			LOG.info("blocksToReplay For replay {}", blocksToReplay);
			String replayBlockNodeData = readNode(pathToNode);
			LOG.info("node data For replay {}", replayBlockNodeData);
			HashMap<String, ?> urlAddressForReplay = getResponseInJson(replayBlockNodeData);
			// LOG.info("urlAddressForReplay For replay {}", urlAddressForReplay);
			responseLink = urlAddressForReplay.get("data") + blocksToReplay;
			LOG.info("responseLink For replay {}", responseLink);
			responseData = HttpUtils.readUrlAsStringWithToken(responseLink, true, requestToken);
			// LOG.info("responseData For replay {}", responseData);
			// LOG.info("Response data {}", responseData);
			dataMap.put("data", responseData);
			// dataMap.put("childrenNodes", getListOfChildNodes(pathToData));
			dataMap.put("Response link", responseLink);

		} catch (Exception e) {
			LOG.error("Error on query for node information for replay {}", pathToData);
		}
		LOG.debug("Response map size: {}", dataMap.size());
		// LOG.debug("Response map Key set {}", dataMap.keySet());
		// LOG.debug("Response map for debugging {}", dataMap);
		return dataMap;
	}

	/**
	 * Method to
	 *
	 * @param data
	 * @return
	 */
	private static String prepareReplayLink(String data) {
		String dataReplay;
		// String[] arrayUrl = data.split("/");
		// for (int i = 0; i < arrayUrl.length - 1; i++) {
		// if (!arrayUrl[i].equals("")) {
		// dataReplay = dataReplay + "/" + arrayUrl[i];
		// }
		// }
		dataReplay = SERVICES_REGISTRY_START_PARENT + data + "_replay";
		return dataReplay;
	}

	/**
	 * Method that finishes building the path to ZooKeeper node, reads the address from node, and returns the data from
	 * the address inside a map object.
	 *
	 * @param pathToData
	 *            The path got on request from front-end or API user to get the dataReading from ZooKeeper node
	 * @return
	 */
	public static HashMap<String, Object> getServiceNodeInfoFromLink(String pathToData, int logLineCount,
			String userToken) throws AuthenticationException {
		setTokenAndGetConn(userToken);
		String responseLink, responseData;
		HashMap<String, Object> dataMap = new HashMap<>(), actionNodes, configMap, responseMap;

		try {
			String requestToken = getTheTokenFromZooKeeper(pathToData, AUTH_NODE_PATH_READ_RIGHTS);
			if (requestToken.length() > 2) {
				requestToken = TOKEN_TYPE + " " + requestToken;
				actionNodes = doChecksForSpecialNeedsNodes(pathToData);
				configMap = getResponseInJson(actionNodes.get("responseLink").toString());
				responseLink = configMap.get("data").toString();
				LOG.info("The link from zkNode: {}", responseLink);
				if (actionNodes.get("token") != null) {
					String token = actionNodes.get("token").toString();
					requestToken = token;
				}
				responseData = HttpUtils.readUrlAsStringWithToken(responseLink, true, requestToken);
				responseMap = getResponseInJson(responseData);
				String value = (String) responseMap.get("dataReading");
				if (value != null) {
					configMap.put("data", value);
				} else {
					if (getAddressEnding(pathToData).equals(ACTIVE_STREAMS_REGISTRY_NODE)) {
						configMap = formatIfMetricsDataStreams(responseMap, pathToData);
					} else if (responseData.charAt(0) == '[') {
						configMap.put("data", responseData);
						configMap = formatIfMetricsData(configMap, pathToData);
						configMap.put("Response link", responseLink);
					} else {
						configMap.put("data", responseMap);
						configMap = formatIfMetricsData(configMap, pathToData);
						configMap.put("Response link", responseLink);
					}
				}

				if (logLineCount != 0) {
					dataMap = getLogLineNumberSpecified(responseData, logLineCount);
				} else {
					dataMap = configMap;
				}
			} else {
				responseLink = SERVICES_REGISTRY_START_PARENT + pathToData;
				String responseInfo = readNode(responseLink);
				dataMap = getResponseInJson(responseInfo);
				dataMap.put("childrenNodes", getListOfChildNodes(SERVICES_REGISTRY_START_PARENT + pathToData));
				dataMap.put("Response link", responseLink);
			}

		} catch (Exception e) {
			LOG.error("Error on query for node information {}", pathToData);
		}
		return dataMap;
	}

	/**
	 * A method to return only the specified number of service log lines from response.
	 *
	 * @param responseData
	 * @param logLineCount
	 * @return
	 */
	private static HashMap<String, Object> getLogLineNumberSpecified(String responseData, int logLineCount) {
		HashMap<String, Object> dataMap;
		List<String> slicedLog;
		HashMap<String, Object> responseMap = new HashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		ObjectWriter writer = objMapper.writer();
		dataMap = getResponseInJson(responseData);
		try {
			String jsonInString2 = writer.writeValueAsString(dataMap.get("data"));
			dataMap = getResponseInJson(jsonInString2);
			responseMap.put("config", dataMap.get("config"));
			ArrayList<String> serviceLog = (ArrayList<String>) dataMap.get("Service log");
			if (logLineCount < serviceLog.size()) {
				slicedLog = serviceLog.subList(serviceLog.size() - logLineCount, serviceLog.size());
				responseMap.put("Service log", slicedLog);
			} else {
				responseMap.put("Service log", serviceLog);
			}
		} catch (JsonProcessingException e) {
			LOG.error("Failed to parse log lines response", e);
		}
		dataMap.clear();
		dataMap.put("data", responseMap);
		return dataMap;
	}

	/**
	 * Method to get the token data from specific node to add to call header
	 *
	 * @param pathToData
	 * @return
	 */
	public static String getTheTokenFromZooKeeper(String pathToData, String tokenNode) {
		pathToData = SERVICES_REGISTRY_START_PARENT + pathToData;
		String[] nodeParts = pathToData.split("/");
		if (nodeParts.length == AGENT_DEPTH) {
			String nodePath = pathToData + "/" + tokenNode;
			return readNode(nodePath);
		} else if (nodeParts.length > AGENT_DEPTH) {
			String nodePath = RequestPath.getPathOfSpecifiedLength(pathToData, AGENT_DEPTH + 1) + tokenNode;
			return readNode(nodePath);
		} else {
			return "";
		}
	}

	/**
	 * Check if the data got is from metrics and if true format according to the method inside ServiceData class
	 *
	 * @param dataMap
	 * @param pathToData
	 * @return
	 */
	private static HashMap<String, Object> formatIfMetricsData(HashMap<String, Object> dataMap, String pathToData) {
		HashMap<String, Object> tempMap = dataMap;
		if (checkIfMetricsData(dataMap)) {
			String serviceName = getStreamName(dataMap);
			tempMap.put("data", getMetricsWithFormatting(dataMap, serviceName));
		}
		tempMap.put("childrenNodes", getListOfChildNodes(SERVICES_REGISTRY_START_PARENT + pathToData));
		// LOG.info("data from data [}", tempMap);
		return tempMap;
	}

	/**
	 * Check if the data got is from metrics and if true format according to the method inside ServiceData class
	 *
	 * @param dataMap
	 * @param pathToData
	 * @return
	 */
	private static HashMap<String, Object> formatIfMetricsDataStreams(HashMap<String, Object> dataMap,
			String pathToData) {
		HashMap<String, Object> tempMap = dataMap;
		if (getAddressEnding(pathToData).equals(ACTIVE_STREAMS_REGISTRY_NODE)) {
			for (Map.Entry<String, Object> stringObjectEntry : dataMap.entrySet()) {
				tempMap.put(stringObjectEntry.getKey(),
						getMetricsWithFormattingStreams(stringObjectEntry.getValue(), stringObjectEntry.getKey()));
			}
		}
		return tempMap;
	}

	/**
	 * Check if simple node or request node, if request node then needs specific handling.
	 *
	 * @param pathToData
	 * @return
	 */
	private static HashMap<String, Object> doChecksForSpecialNeedsNodes(String pathToData)
			throws JsonProcessingException {
		String responseLink;
		HashMap<String, Object> respone = new HashMap<>(), configMap;
		boolean actionCall = false;
		String tempPathToNode = SERVICES_REGISTRY_START_PARENT + pathToData;
		if (checkIfNeededURLDownload(pathToData, "downloadables")) {
			tempPathToNode = RequestPath.getPathOfSpecifiedLength(tempPathToNode, 8);
			tempPathToNode = tempPathToNode.substring(0, tempPathToNode.length() - 1);
			responseLink = readNode(tempPathToNode);
			respone.put("responseLink", responseLink);
			configMap = getResponseInJson(responseLink);
			responseLink = configMap.get("data").toString();
			responseLink = responseLink + "/" + getAddressEnding(pathToData);

			ObjectMapper objMapper = new ObjectMapper();
			ObjectWriter writer = objMapper.writer();
			configMap.put("data", responseLink);
			responseLink = writer.writeValueAsString(configMap);
		} else if (checkIfNeededURLControls(pathToData, "_stop") || checkIfNeededURLControls(pathToData, "_start")) {
			actionCall = true;
			responseLink = readNode(tempPathToNode);
		} else {
			actionCall = false;
			responseLink = readNode(tempPathToNode);
		}
		if (actionCall) {
			String token = getTheTokenFromZooKeeper(pathToData, AUTH_NODE_PATH_ACTION_RIGHTS);
			token = TOKEN_TYPE + " " + token;
			LOG.info("Action token from ZooKeeper {}", token);
			respone.put("token", token);
		}
		respone.put("responseLink", responseLink);
		// LOG.info("Response link that makes the call to ZooKeeper REST " + responseLink);
		return respone;
	}

	/**
	 * Returns the last element in string path
	 *
	 * @param path
	 * @return
	 */
	public static String getAddressEnding(String path) {
		String pathEnding = path.substring(path.lastIndexOf('/') + 1);
		return pathEnding;
	}

	/**
	 * Returns a list of children nodes to be added to teh main response map
	 *
	 * @param parentPath
	 * @return
	 */
	private static List<String> getListOfChildNodes(String parentPath) {
		Collection<String> nodeNames;
		List<String> neededNames = new ArrayList<>();
		ServiceDiscovery<String> serviceDiscovery = null;
		try {
			Stat statResponse = client.checkExists().forPath(parentPath);
			if (statResponse == null) {
				LOG.info("No node exists for path. Check the call URL or ZooKeeper node tree {}", parentPath);
			} else {
				serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client).basePath(parentPath)
						.build();
				serviceDiscovery.start();
				nodeNames = serviceDiscovery.queryForNames();
				for (String nodeChildName : nodeNames) {
					String nodeName = getAddressEnding(nodeChildName);
					if (nodeName.charAt(0) == '_') {
						// LOG.info("The node {} does not need to be shown in tree view {}", nodeChildName);
					} else {
						neededNames.add(nodeChildName);
					}
				}
			}
			serviceDiscovery.close();
		} catch (Exception e) {
			LOG.error("Error on getting the children of parent nodes", e);
		}
		return neededNames;
	}

	/**
	 * A method used to take the zooKeeper connection with rights depending on client.
	 * 
	 * @param token
	 * @throws AuthenticationException
	 */
	private static void setTokenAndGetConn(String token) throws AuthenticationException {
		zooManager.setConnectionToken(token);
		client = zooManager.getClientConnection();
	}

	// ----------- Rundeck jobs ---------

	/**
	 * Method for executing jobs by calling Rundeck API
	 *
	 * @param pathToData
	 * @param userToken
	 * @return
	 * @throws AuthenticationException
	 */
	public static HashMap<String, Object> executeRundeckJob(String pathToData, String userToken)
			throws AuthenticationException {
		setTokenAndGetConn(userToken); // Set the connection to the current connected client
		HashMap<String, Object> dataMap = new HashMap<>(); // For response (Json)
		String responseLink, responseData, pathToNode, blocksToReplay;

		try {
			String[] nodeParts = pathToData.split("/");
			String requestToken = getTheTokenFromZooKeeper(pathToData, AUTH_NODE_PATH_ACTION_RIGHTS);
			requestToken = TOKEN_TYPE + " " + requestToken;
			if (nodeParts.length > AGENT_DEPTH) {
				pathToNode = RequestPath.getPathOfSpecifiedLength(pathToData, AGENT_DEPTH + 1);
			} else {
				pathToNode = RequestPath.getPathOfSpecifiedLength(pathToData, AGENT_DEPTH + 1);
			}
			pathToNode = prepareReplayLink(pathToNode);
			blocksToReplay = getAddressEnding(pathToData);
			responseLink = readNode(pathToNode) + blocksToReplay;
			responseData = HttpUtils.readUrlAsStringWithToken(responseLink, true, requestToken);
			// LOG.info("Response data {}", responseData);
			dataMap = getResponseInJson(responseData);
			dataMap.put("childrenNodes", getListOfChildNodes(pathToData));
			dataMap.put("Response link", responseLink);

		} catch (Exception e) {
			LOG.error("Error on query for node information for rundeck {}", pathToData, e);
		}
		LOG.debug("Response map size: {}", dataMap.size());
		// LOG.debug("Response map Key set {}", dataMap.keySet());
		// LOG.debug("Response map for debugging {}", dataMap);
		return dataMap;
	}
}