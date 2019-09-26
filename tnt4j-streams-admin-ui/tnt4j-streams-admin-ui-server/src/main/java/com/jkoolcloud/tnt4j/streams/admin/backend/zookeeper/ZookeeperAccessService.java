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
import org.apache.log4j.Logger;
import org.apache.zookeeper.data.Stat;

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
	private static Logger LOG = Logger.getLogger(ZookeeperAccessService.class);
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
			if (!sslConfigFilePath.equals("") && !sslPass.equals("")) {
				System.setProperty("javax.net.ssl.trustStore", sslConfigFilePath);
				System.setProperty("javax.net.ssl.trustStorePassword", sslPass);
			}
//			if (credentials == null || credentials.isEmpty()) {
//				destroy();
//				LOG.info("No credentials no connection to ZooKeeper " + " ZOOKEEPER_URL");
//			}
			if (!client.getState().toString().equals("STARTED")) {
				LOG.info("CLIENT NOT YET STARTED: ");
				client.start();
			}else{
				LOG.info("CLIENT STARTED: ");
			}
		} catch (Exception e) {
			LOG.error("Error on Zookeeper connection start" + e);
		}
	}

	public static CuratorFramework getConnection() throws AuthenticationException {
		return zooManager.getClientConnection();
	}

	public static CuratorFramework getConnectionAdmin() {
		String ZOOKEEPER_URL = null;
		CuratorFramework admin = null;
		try {
			String credentialsAdmin = PropertyData.getProperty("UserManagerUsername") + ":" + PropertyData.getProperty("UserManagerPassword");
			ZOOKEEPER_URL = PropertyData.getProperty("ZooKeeperAddress");
			LOG.info("Credentials for admin connection: " + credentials);

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
	public static HashMap getResponseInJson(String data) {
		HashMap dataMap = new HashMap<>();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			if(data.charAt(0) == '[') {
				dataMap.put("data", data);
			} else if (JsonRpc.isJSONValid(data)) {
				dataMap = objectMapper.readValue(data, HashMap.class);
			} else {
				dataMap.put("dataReading", data);
			}
		} catch (Exception e) {
			LOG.error("Error on putting data into a map object");
			LOG.error("Error" + e);
		}
		return dataMap;
	}

	/**
	 * Get the map dataReading ant format it using the method prom service dataReading
	 *
	 * @param dataMap The dataReading from ZooKeeper node inside Map
	 * @return
	 */
	private static Map getMetricsWithFormattingStreams(Object dataMap, String serviceName) {
		Map tempMap = null;
		HashMap tempData = new HashMap();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String json = objectMapper.writeValueAsString(dataMap);
			dataMap = objectMapper.readValue(json, HashMap.class);
			tempData.put(serviceName, dataMap);
			tempMap = ServiceData.parseJsonDataIntoSimpleFormatZooKeeper(tempData, serviceName);
		} catch (Exception e) {
			LOG.error("Problem while trying to parse metrics dataReading" + e);
		}
		return tempMap;
	}

	/**
	 * Get the map dataReading ant format it using the method prom service dataReading
	 *
	 * @param dataMap The dataReading from ZooKeeper node inside Map
	 * @return
	 */
	private static Map getMetricsWithFormatting(HashMap dataMap, String serviceName) {
		Map tempMap = null;
		try {
			tempMap = ServiceData.parseJsonDataIntoSimpleFormatZooKeeper(dataMap, serviceName);
			//LOG.info("Formatted metrics dataReading "+ tempMap);

		} catch (Exception e) {
			LOG.error("Problem while trying to parse metrics dataReading" + e);
		}
		return tempMap;
	}

	/**
	 * Check if the dataReading call is for metrics dataReading
	 *
	 * @param dataMap The dataReading from ZooKeeper node inside Map
	 * @return
	 */
	private static boolean checkIfMetricsData(HashMap dataMap) {
		HashMap configData = (HashMap) dataMap.get("config");
		if (configData != null) {
			if (configData.get("componentLoad").toString().equals("metrics")
					|| configData.get("componentLoad").toString().equals("service")) {
				return true;
			} else {
				return false;
			}
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
			LOG.error("There was a problem while checking if the call is from {} node {} " + pathToData, e);
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
			LOG.error("There was a problem while checking if the call is from {} node {} " + pathToData, e);
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
			LOG.error("There was a problem while checking if the call is from {} node {} " + pathToData, e);
		}
		return false;
	}

	/**
	 * Get service name from inside the metrics information
	 *
	 * @param dataMap The dataReading from ZooKeeper node inside Map
	 * @return
	 */
	private static String getStreamName(HashMap dataMap) {
		HashMap serviceData = (HashMap) dataMap.get("data");
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

	public void connectToZooKeeper(String credentialsSent) {
		String ZOOKEEPER_URL = null;
		try {
			client = zooManager.addClientConnection(credentialsSent);
			init();
//			ZOOKEEPER_URL = PropertyData.getProperty("ZooKeeperAddress");
//			LOG.info("Connecting to Zookeeper at " + ZOOKEEPER_URL);
//			builder = CuratorFrameworkFactory.builder().connectString(ZOOKEEPER_URL)
//					.retryPolicy(new ExponentialBackoffRetry(1000, 3))
//					.authorization("digest", credentialsSent.getBytes());
//			client = builder.build();
			credentials = credentialsSent;

//		} catch (IOException e) {
//			LOG.error("Problem on reading properties file information");
//			e.printStackTrace();
		} catch (Exception e) {
			LOG.error("An unexpected error occurred on connection iwth credentials");
			e.printStackTrace();
		}
	}

	/**
	 * Read data from the provided node
	 *
	 * @param nodePath
	 * @return
	 */
	public String readNode(String nodePath) {
		String responseData = "";
		try {
			byte[] nodeLinkBytes = client.getData().watched().forPath(nodePath);
			responseData = new String(nodeLinkBytes);
//			LOG.info("Node data: "+ responseData);
		} catch (Exception e) {
			LOG.error("Error on query for node information " + nodePath);
			LOG.error("Error" + e);
		}
		return responseData;
	}

	/**
	 * Use to check if the credentials validation was successful and if the user has admin rights
	 */
	public boolean checkIfConnected(){
		LoginCache cache = new LoginCache();
        Boolean userConnected = false, userAdmin, userAction;
			if (client.getState() == CuratorFrameworkState.STARTED) {
				Collection<String> clusterNodes = CuratorUtils.nodeChildrenList(SERVICES_REGISTRY_START_NODE, client);
				for (String cluster : clusterNodes) {
					String tempClusterNode = SERVICES_REGISTRY_START_NODE+"/"+cluster;
					try {
						String NodeData = readNode(SERVICES_REGISTRY_START_NODE+"/"+cluster);
						if (NodeData.isEmpty()) {
						} else{
							if(!userConnected) {
								userConnected = true;
								String tokenNeeded = cache.generateTokenForUser();
								zooManager.setConnectionToken(tokenNeeded);
								zooManager.setClientConnection(client);
							}
							userAdmin = CuratorUtils.checkIfUserIsAdmin(client, tempClusterNode, credentials);
							if(userAdmin){ cache.setIsUserAdmin(true); }
						}
					}catch(Exception e){
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
	public Map<String, String> getTreeNodes(String userToken) throws AuthenticationException {
		setTokenAndGetConn(userToken);
		Map<String, String> myNodeMap = new HashMap<>();
		String parentNode = "BaseNode";
		int nodeLevel = 0;
		Map<String, String> nodeMap = getZooKeeperTreeNodes(SERVICES_REGISTRY_START_NODE, myNodeMap, parentNode,
				nodeLevel);
//		LOG.info("ZooKeeper Node Tree Map = "+ nodeMap.size());
		return nodeMap;
	}

	/**
	 * Method that uses recursion to get all the needed nodes from ZooKeeper
	 *
	 * @param path       The starting ZooKeeper node
	 * @param myNodeMap  Node map
	 * @param parentNode The node for saving the parent node value with current node "initial : BaseNode"
	 * @return The map of all nodes together with their parent nodes
	 */
	private Map<String, String> getZooKeeperTreeNodes(String path, Map<String, String> myNodeMap, String parentNode,
													  int nodeLevel) {
		try {
			Collection<String> serviceNames = null;
			String nodeName = getAddressEnding(path);
			Stat statResponse = client.checkExists().forPath(path);
			if (statResponse == null) {
				LOG.info("No call path exists: {} Check the configuration file " + path);
			} else if (nodeName.charAt(0) == '_') {
//				LOG.info("The node {} does not need to be shown in tree view "+ path);
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
	 * @param pathToData The path got on request from front-end or API user to get the dataReading from ZooKeeper node
	 * @return
	 */
	public HashMap getServiceNodeInfoFromLinkForReplay(String pathToData, String userToken) throws AuthenticationException {
		setTokenAndGetConn(userToken);
		String responseLink, responseData, pathToNode, blocksToReplay;
		HashMap dataMap = new HashMap();
		try {
			String[] nodeParts = pathToData.split("/");
			String requestToken = getTheTokenFromZooKeeper(pathToData, AUTH_NODE_PATH_ACTION_RIGHTS);
			requestToken = TOKEN_TYPE + " " + requestToken;
			if (nodeParts.length > AGENT_DEPTH) {
				pathToNode = RequestPath.getPathOfSpecifiedLength(pathToData, AGENT_DEPTH + 1);
			} else {
				pathToNode = RequestPath.getPathOfSpecifiedLength(pathToData, AGENT_DEPTH + 1);
			}
			pathToNode = prepareReplayLink(pathToNode);// doChecksForSpecialNeedsNodes(pathToData);
			blocksToReplay = getAddressEnding(pathToData);
			responseLink = readNode(pathToNode) + blocksToReplay;
			responseData = HttpUtils.readUrlAsStringWithToken(responseLink, true, requestToken);
			//LOG.info("Response data "+ responseData);
			dataMap = getResponseInJson(responseData);
			dataMap.put("childrenNodes", getListOfChildNodes(pathToData));
			dataMap.put("Response link", responseLink);

		} catch (Exception e) {
			LOG.error("Error on query for node information " + pathToData);
			LOG.error("Error", e);
		}
		LOG.debug("Response map size: " + dataMap.size());
//		LOG.debug("Response map Key set "+ dataMap.keySet());
		//LOG.debug("Response map for debuging "+ dataMap);
		return dataMap;
	}

	/**
	 * Method to
	 *
	 * @param data
	 * @return
	 */
	private String prepareReplayLink(String data) {
		String dataReplay = "";
		String[] arrayUrl = data.split("/");
		for (int i = 0; i < arrayUrl.length - 1; i++) {
			if (!arrayUrl[i].equals("")) {
				dataReplay = dataReplay + "/" + arrayUrl[i];
			}
		}
		dataReplay = SERVICES_REGISTRY_START_PARENT + dataReplay + "/_replay";
		return dataReplay;
	}

	/**
	 * Method that finishes building the path to ZooKeeper node, reads the address from node, and returns the data from
	 * the address inside a map object.
	 *
	 * @param pathToData The path got on request from front-end or API user to get the dataReading from ZooKeeper node
	 * @return
	 */
	public HashMap getServiceNodeInfoFromLink(String pathToData, int logLineCount, String userToken) throws AuthenticationException {
		setTokenAndGetConn(userToken);
		String responseLink, responseData;
		HashMap dataMap = new HashMap(), actionNodes, configMap, responseMap;

		try {
			String requestToken = getTheTokenFromZooKeeper(pathToData, AUTH_NODE_PATH_READ_RIGHTS);
			if (requestToken.length() > 2) {
				requestToken = TOKEN_TYPE + " " + requestToken;
				actionNodes = doChecksForSpecialNeedsNodes(pathToData);
				configMap = getResponseInJson(actionNodes.get("responseLink").toString());
				responseLink = configMap.get("data").toString();
				LOG.info("The link from zkNode: "+responseLink);
				if(actionNodes.get("token") != null){
					String token = actionNodes.get("token").toString();
					requestToken = token;
				}
				responseData = HttpUtils.readUrlAsStringWithToken(responseLink, true, requestToken);
				responseMap = getResponseInJson(responseData);
				String value = (String) responseMap.get("dataReading");
				if(value != null) {
					configMap.put("data",value);
				}else {
					if (getAddressEnding(pathToData).equals(ACTIVE_STREAMS_REGISTRY_NODE)) {
						configMap = formatIfMetricsDataStreams(responseMap, pathToData);
					} else if(responseData.charAt(0) == '['){
						configMap.put("data", responseData);
						configMap = formatIfMetricsData(configMap, pathToData);
						configMap.put("Response link", responseLink);
					} else {
						configMap.put("data", responseMap);
						configMap = formatIfMetricsData(configMap, pathToData);
						configMap.put("Response link", responseLink);
					}
				}
				//responseData = "{\"data\":{\"Service error log\":[],\"Service log\":[\"2019-08-20 10:17:18,017| INFO\", \"|com.jkoolcloud.tnt4j.streams.inputs.RestStream| =>  Invoking RESTful service POST request:\", \" url=https://mainnet.infura.io/v3/5fc47c37ebd24bc68c4f203742da9752, reqData= | RUNTIME=24964@EC2AMAZ-8CBM9A6#SERVER=EC2AMAZ\", \"-8CBM9A6#NETADDR=172.31.45.251#DATACENTER=UNKNOWN#GEOADDR=UNKNOWNrn\"],\"config\":{\"componentLoad\":\"logs\",\"streamsIcon\":\"<svg height='2.5em' id='svg8' version='1.1' viewBox='2 2 9 9' xmlns='http://www.w3.org/2000/svg' xmlns:cc='http://creativecommons.org/ns#' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:svg='http://www.w3.org/2000/svg'><defs id='defs2'/><g id='layer1' transform='translate(0,-284.29998)'><path d='m 4.9388885,287.12215 4.9388893,-2e-5 v 0.7056 H 4.9388885 Z' id='path4487' style='fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 4.9388888,288.53327 4.9388892,-2e-5 v 0.7056 H 4.9388888 Z' id='path4507' style='fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 4.9388888,289.94442 0.7055559,-5e-5 v 0.70557 l -0.7055559,8e-5 z' id='path4511' style='fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 4.9388888,291.35554 h 0.7055559 v 0.70555 H 4.9388888 Z' id='path4513' style='fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 4.9388888,292.76661 h 0.7055559 v 0.70557 H 4.9388888 Z' id='path4517' style='fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 2.8222225,287.12213 h 1.4111108 v 0.70552 H 2.8222225 Z' id='rect4537' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><path d='m 2.8222225,288.53325 h 1.4111108 v 0.70554 H 2.8222225 Z' id='path4540' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><path d='m 2.8222225,289.94442 h 1.4111108 v 0.70552 H 2.8222225 Z' id='path4544' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><path d='m 2.8222225,291.35554 h 1.4111108 v 0.70552 H 2.8222225 Z' id='path4546' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><path d='m 2.8222225,292.76666 h 1.4111108 v 0.70552 H 2.8222225 Z' id='path4548' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><path d='M 10.301112,293.89583 9.1722226,292.7669' id='path4562' style='fill:none;stroke:#000000;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 8.1138888,289.94471 a 1.7638888,1.7638888 0 0 0 -1.7638887,1.76387 1.7638888,1.7638888 0 0 0 1.7638887,1.76389 1.7638888,1.7638888 0 0 0 1.7638892,-1.76389 1.7638888,1.7638888 0 0 0 -1.7638892,-1.76387 z m 0,0.70552 a 1.0583334,1.0583334 0 0 1 1.0583336,1.05835 1.0583334,1.0583334 0 0 1 -1.0583336,1.05832 1.0583334,1.0583334 0 0 1 -1.0583332,-1.05832 1.0583334,1.0583334 0 0 1 1.0583332,-1.05835 z' id='path4564' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><rect height='0.70555556' id='rect4599' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:0.37647059;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1' width='0.70555556' x='4.2333331' y='287.1221'/><rect height='0.70555556' id='rect4601' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:0.37647059;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1' width='0.70555556' x='4.2333331' y='288.53323'/><rect height='0.70555556' id='rect4605' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:0.37647059;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1' width='0.70555556' x='4.2333331' y='289.94437'/><rect height='0.70555556' id='rect4607' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:0.37647059;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1' width='0.70555556' x='4.2333331' y='291.3555'/><rect height='0.70555556' id='rect4609' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:0.37647059;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1' width='0.70555556' x='4.2333331' y='292.76663'/></g></svg>\"}}}\n" + "\t";
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
			LOG.error("Error on query for node information " + pathToData);
			LOG.error("Error", e);
		}
//		LOG.debug("Response map size: " + dataMap.size());
//		LOG.info("The data from zkNode: "+dataMap);
		return dataMap;
	}

	/**
	 * A method to return only the specified number of service log lines from response.
	 *
	 * @param responseData
	 * @param logLineCount
	 * @return
	 */
	private HashMap getLogLineNumberSpecified(String responseData, int logLineCount) {
		HashMap dataMap;
		List<String> slicedLog;
		HashMap responseMap = new HashMap();
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
			e.printStackTrace();
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
	public String getTheTokenFromZooKeeper(String pathToData, String tokenNode) {
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
	private HashMap formatIfMetricsData(HashMap dataMap, String pathToData) {
		HashMap tempMap = dataMap;
		if (checkIfMetricsData(dataMap)) {
			String serviceName = getStreamName(dataMap);
			tempMap.put("data", getMetricsWithFormatting(dataMap, serviceName));
		}
		tempMap.put("childrenNodes", getListOfChildNodes(SERVICES_REGISTRY_START_PARENT + pathToData));
		//LOG.info("data from data [}", tempMap);
		return tempMap;
	}

	/**
	 * Check if the data got is from metrics and if true format according to the method inside ServiceData class
	 *
	 * @param dataMap
	 * @param pathToData
	 * @return
	 */
	private HashMap formatIfMetricsDataStreams(HashMap dataMap, String pathToData) {
		HashMap tempMap = dataMap;
		if (getAddressEnding(pathToData).equals(ACTIVE_STREAMS_REGISTRY_NODE)) {
			for (Object serviceName : dataMap.keySet()) {

				tempMap.put(serviceName, getMetricsWithFormattingStreams(dataMap.get(serviceName), serviceName.toString()));
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
	private HashMap doChecksForSpecialNeedsNodes(String pathToData) throws JsonProcessingException {
		String responseLink = "";
		HashMap respone = new HashMap(), configMap;
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
			LOG.info("Action token from ZooKeeper" + token);
			respone.put("token", token);
		}
		respone.put("responseLink", responseLink);
//		LOG.info("Response link that makes the call to ZooKeeper REST " + responseLink);
		return respone;
	}

	/**
	 * Returns the last element in string path
	 *
	 * @param path
	 * @return
	 */
	public String getAddressEnding(String path) {
		String pathEnding = path.substring(path.lastIndexOf('/') + 1);
		return pathEnding;
	}

	/**
	 * Returns a list of children nodes to be added to teh main response map
	 *
	 * @param parentPath
	 * @return
	 */
	private List<String> getListOfChildNodes(String parentPath) {
		Collection<String> nodeNames = null;
		List<String> neededNames = new ArrayList<>();
		ServiceDiscovery<String> serviceDiscovery = null;
		try {
			Stat statResponse = client.checkExists().forPath(parentPath);
			if (statResponse == null) {
				LOG.info("No node exists for path. Check the call URL or ZooKeeper node tree " + parentPath);
			} else {
				serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client)
						.basePath(parentPath).build();
				serviceDiscovery.start();
				nodeNames = serviceDiscovery.queryForNames();
				for (String nodeChildName : nodeNames) {
					String nodeName = getAddressEnding(nodeChildName);
					if (nodeName.charAt(0) == '_') {
//						LOG.info("The node {} does not need to be shown in tree view "+ nodeChildName);
					} else {
						neededNames.add(nodeChildName);
					}
				}
			}
			serviceDiscovery.close();
		} catch (Exception e) {
			LOG.error("Error on getting the children of parent nodes" + e);
		}
		return neededNames;
	}

	private void setTokenAndGetConn(String token) throws AuthenticationException {
		zooManager.setConnectionToken(token);
		client = zooManager.getClientConnection();
	}
}