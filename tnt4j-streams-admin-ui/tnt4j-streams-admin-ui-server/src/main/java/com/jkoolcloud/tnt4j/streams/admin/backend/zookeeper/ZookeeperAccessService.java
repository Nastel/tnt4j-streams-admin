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
    private static  String AUTH_NODE_PATH_ACTION_RIGHTS, AUTH_NODE_PATH_READ_RIGHTS, SERVICES_REGISTRY_START_NODE,
			SERVICES_REGISTRY_START_PARENT, ACTIVE_STREAMS_REGISTRY_NODE, SSL_FILE_CONF, credentials, TOKEN_TYPE;

	private static CuratorFrameworkFactory.Builder builder;
	private static CuratorFramework client;
	private static int AGENT_DEPTH;



	/**
	 * The entry point of application.
	 *
	 * @param args
	 *            the input arguments
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String[] args) throws Exception {
		ZookeeperAccessService zooAccess = new ZookeeperAccessService();
		//ZookeeperAccessService.init();
		zooAccess.connectToZooKeeper("digest:admin:admin");
		zooAccess.getTreeNodes();

		LOG.debug("Hello this is a debug message");
		LOG.info("Hello this is an info message");
//		zooAccess.checkIfConnected();
//		zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream2");
//		zooAccess.getListOfChildNodesWithChildren("/streams/v1/clusters/clusterBlockchainMainnets/streamsAgentEth");
//		zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/threadDump");
//		zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth");
//		zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/downloadables/streamAdminLogger.log");
//		zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/downloadables/streamAdminLogger.log");
//      zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/downloadables/tnt4j-streams-activities.log");
      zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth",0);
//		zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/sampleConfigurations");
//		zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/_streamsAndMetrics", 0);
//		zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream2", 0);
//		zooAccess.getServiceNodeInfoFromLinkForReplay("/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream2/8316044");
//		zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/sampleConfigurations", 0);
//		zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/downloadables/file.txt");
//		zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/logs", 3);
//		zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream2/_start", 0);
//		zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets");
		zooAccess.destroy();
	}

	/**
	 * Init ZooKeeper connection.
	 */
	@PostConstruct
	private static void init() {
		String ZOOKEEPER_URL = null;
		try {
			SERVICES_REGISTRY_START_NODE = PropertyData.getProperty("serviceRegistryStartNode");
			SERVICES_REGISTRY_START_PARENT = PropertyData.getProperty("serviceRegistryStartNodeParent");
            ACTIVE_STREAMS_REGISTRY_NODE = PropertyData.getProperty("activeStreamRegistry");
			AUTH_NODE_PATH_ACTION_RIGHTS = PropertyData.getProperty("authorizationTokenAction");
			AUTH_NODE_PATH_READ_RIGHTS = PropertyData.getProperty("authorizationTokenRead");
			AGENT_DEPTH = Integer.parseInt(PropertyData.getProperty("depthToAgentNode"));
			TOKEN_TYPE = PropertyData.getProperty("tokenType");
			SSL_FILE_CONF = PropertyData.getProperty("SslConfigFilePath");
//			System.setProperty("javax.net.ssl.trustStore","C:\\Program Files\\Java\\jre1.8.0_201\\lib\\security\\cacerts");
//			System.setProperty("javax.net.ssl.trustStorePassword","changeit");
//			System.setProperty("javax.net.ssl.trustStore",SSL_FILE_CONF);
//			System.setProperty("javax.net.ssl.trustStorePassword","27RZfaGhSR");
			if (credentials == null || credentials.isEmpty()) {
				destroy();
				LOG.info("No credentials no connection to ZooKeeper "+" ZOOKEEPER_URL");
			}
			if(!client.getState().toString().equals("STARTED")){
				client.start();
			}
		} catch (Exception e) {
			LOG.error("Error on Zookeeper connection start"+ e);
		}
	}

	public static CuratorFramework getConnection(){
		return client;
	}

	public static CuratorFramework getConnectionAdmin() {
		String ZOOKEEPER_URL = null;
		CuratorFramework admin = null;
		try {
			String credentialsAdmin = PropertyData.getProperty("UserManagerUsername") + ":" + PropertyData.getProperty("UserManagerPassword");
			ZOOKEEPER_URL = PropertyData.getProperty("ZooKeeperAddress");
			LOG.info("Credentials for admin connection: "+credentials);

			builder = CuratorFrameworkFactory.builder().connectString(ZOOKEEPER_URL)
				.retryPolicy(new ExponentialBackoffRetry(1000, 3))
				.authorization("digest", credentialsAdmin.getBytes());
			admin = builder.build();

			LOG.info("Connection start admin");
			admin.start();
			init();
		}catch (IOException e){
			LOG.info("problem on reading property data");
		}
		return admin;
	}

	public static void stopConnectionAdmin(CuratorFramework adminConn){
		if (adminConn != null) {
			try {
				adminConn.close();
			} catch (Exception e) {
				LOG.error("Error on zookeeper disconnect");
			}
		}
	}
	public static void connectToZooKeeper(String credentialsSent){
		String ZOOKEEPER_URL = null;
		try {
			ZOOKEEPER_URL = PropertyData.getProperty("ZooKeeperAddress");
			LOG.info("Connecting to Zookeeper at "+ ZOOKEEPER_URL);
			builder = CuratorFrameworkFactory.builder().connectString(ZOOKEEPER_URL)
					.retryPolicy(new ExponentialBackoffRetry(1000, 3))
					.authorization("digest", credentialsSent.getBytes());
			client = builder.build();
			credentials = credentialsSent;
			init();
		} catch (IOException e) {
			LOG.error("Problem on reading properties file information");
			e.printStackTrace();
		} catch (Exception e){
			LOG.error("An unexpected error occurred on connection iwth credentials");
			e.printStackTrace();
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
			if (JsonRpc.isJSONValid(data)) {
				ObjectMapper objectMapper = new ObjectMapper();
				dataMap = objectMapper.readValue(data, HashMap.class);
			} else {
				dataMap.put("dataReading", data);
			}
		} catch (Exception e) {
			LOG.error("Error on putting data into a map object");
			LOG.error("Error"+ e);
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
			LOG.error("Problem while trying to parse metrics dataReading"+ e);
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
	private static Map getMetricsWithFormatting(HashMap dataMap, String serviceName) {
		Map tempMap = null;
		try {
			tempMap = ServiceData.parseJsonDataIntoSimpleFormatZooKeeper(dataMap, serviceName);
			//LOG.info("Formatted metrics dataReading "+ tempMap);

		} catch (Exception e) {
			LOG.error("Problem while trying to parse metrics dataReading"+ e);
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
			LOG.error("There was a problem while checking if the call is from {} node {} "+ pathToData, e);
		}
		return false;
	}

	/**
	 * Checks if the provided path is a request to download a file.
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
			LOG.error("There was a problem while checking if the call is from {} node {} "+ pathToData, e);
		}
		return false;
	}

    /**
     * Checks if the provided path is a request to download a file.
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
			LOG.error("There was a problem while checking if the call is from {} node {} "+ pathToData, e);
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
			LOG.error("Error on query for node information "+ nodePath);
			LOG.error("Error"+ e);
		}
		return responseData;
	}

	/**
	 * Use to check if the credentials validation was successfull
	 */
	public boolean checkIfConnected(){
		LoginCache cache = new LoginCache();
        Collection<String> clusterChildren= null;
        Boolean userConnected = false, userAdmin, userAction;
//        LOG.info("Client state check: "+ client.getState());
			if (client.getState() == CuratorFrameworkState.STARTED) {
				Collection<String> clusterNodes = CuratorUtils.nodeChildrenList(SERVICES_REGISTRY_START_NODE, client);
//				LOG.info("LIst of children nodes: "+ clusterNodes);
				for (String cluster : clusterNodes) {
					String tempClusterNode = SERVICES_REGISTRY_START_NODE+"/"+cluster;
					try {
						String NodeData = readNode(SERVICES_REGISTRY_START_NODE+"/"+cluster);
//						LOG.info("Node information, "+ SERVICES_REGISTRY_START_NODE+"/"+cluster + " NOde data: "+ NodeData );
						clusterChildren = CuratorUtils.nodeChildrenList(tempClusterNode, client);
//						LOG.info("Node information, "+NodeData.isEmpty() +" "+ clusterChildren.isEmpty() );
						if (NodeData.isEmpty()) {
						} else{
							if(!userConnected) { userConnected = true; }
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
	public Map<String, String> getTreeNodes() {
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
	 * @param path
	 *            The starting ZooKeeper node
	 * @param myNodeMap
	 *            Node map
	 * @param parentNode
	 *            The node for saving the parent node value with current node "initial : BaseNode"
	 * @return The map of all nodes together with their parent nodes
	 */
	private Map<String, String> getZooKeeperTreeNodes(String path, Map<String, String> myNodeMap, String parentNode,
			int nodeLevel) {
		try {
            Collection<String> serviceNames = null;
			String nodeName = getAddressEnding(path);
			Stat statResponse = client.checkExists().forPath(path);
			if (statResponse == null) {
				LOG.info("No call path exists: {} Check the configuration file "+ path);
			}
			else if(nodeName.charAt(0)=='_'){
//				LOG.info("The node {} does not need to be shown in tree view "+ path);
			}else {
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
			LOG.error("Error on tree creation", e);
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
	public HashMap getServiceNodeInfoFromLinkForReplay(String pathToData) {
		String responseLink, responseData, pathToNode, blocksToReplay;
		HashMap dataMap = new HashMap();
		try {
			String[] nodeParts = pathToData.split("/");
			String requestToken = getTheTokenFromZooKeeper(pathToData, AUTH_NODE_PATH_ACTION_RIGHTS);
			requestToken =TOKEN_TYPE + " "+requestToken;
		    if(nodeParts.length>AGENT_DEPTH){
				pathToNode =  RequestPath.getPathOfSpecifiedLength(pathToData, AGENT_DEPTH+1);
			}else{
				pathToNode =  RequestPath.getPathOfSpecifiedLength(pathToData, AGENT_DEPTH+1);
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
			LOG.error("Error on query for node information "+ pathToData);
			LOG.error("Error", e);
		}
		LOG.debug("Response map size: "+ dataMap.size());
//		LOG.debug("Response map Key set "+ dataMap.keySet());
		//LOG.debug("Response map for debuging "+ dataMap);
		return dataMap;
	}

	/**
	 *  Method to
	 * @param data
	 * @return
	 */
	private String prepareReplayLink(String data) {
		String dataReplay ="";
		String[] arrayUrl = data.split("/");
		for (int i=0; i<arrayUrl.length-1; i++) {
			if(!arrayUrl[i].equals("")) {
				dataReplay = dataReplay+"/"+arrayUrl[i];
			}
		}
		dataReplay =SERVICES_REGISTRY_START_PARENT +dataReplay+"/_replay";
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
	public HashMap getServiceNodeInfoFromLink(String pathToData, int logLineCount) {

		String responseLink, responseData;
		HashMap dataMap = new HashMap(), actionNodes, configMap, responseMap;

		try {
			String requestToken = getTheTokenFromZooKeeper(pathToData, AUTH_NODE_PATH_READ_RIGHTS);
            if(requestToken.length() > 2) {
				requestToken =TOKEN_TYPE + " "+requestToken;
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
					configMap.put("data", "");
				}else {
					configMap.put("data", responseMap);
				}
				//responseData = "{\"data\":{\"Service error log\":[],\"Service log\":[\"2019-08-20 10:17:18,017| INFO\", \"|com.jkoolcloud.tnt4j.streams.inputs.RestStream| =>  Invoking RESTful service POST request:\", \" url=https://mainnet.infura.io/v3/5fc47c37ebd24bc68c4f203742da9752, reqData= | RUNTIME=24964@EC2AMAZ-8CBM9A6#SERVER=EC2AMAZ\", \"-8CBM9A6#NETADDR=172.31.45.251#DATACENTER=UNKNOWN#GEOADDR=UNKNOWNrn\"],\"config\":{\"componentLoad\":\"logs\",\"streamsIcon\":\"<svg height='2.5em' id='svg8' version='1.1' viewBox='2 2 9 9' xmlns='http://www.w3.org/2000/svg' xmlns:cc='http://creativecommons.org/ns#' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:svg='http://www.w3.org/2000/svg'><defs id='defs2'/><g id='layer1' transform='translate(0,-284.29998)'><path d='m 4.9388885,287.12215 4.9388893,-2e-5 v 0.7056 H 4.9388885 Z' id='path4487' style='fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 4.9388888,288.53327 4.9388892,-2e-5 v 0.7056 H 4.9388888 Z' id='path4507' style='fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 4.9388888,289.94442 0.7055559,-5e-5 v 0.70557 l -0.7055559,8e-5 z' id='path4511' style='fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 4.9388888,291.35554 h 0.7055559 v 0.70555 H 4.9388888 Z' id='path4513' style='fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 4.9388888,292.76661 h 0.7055559 v 0.70557 H 4.9388888 Z' id='path4517' style='fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 2.8222225,287.12213 h 1.4111108 v 0.70552 H 2.8222225 Z' id='rect4537' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><path d='m 2.8222225,288.53325 h 1.4111108 v 0.70554 H 2.8222225 Z' id='path4540' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><path d='m 2.8222225,289.94442 h 1.4111108 v 0.70552 H 2.8222225 Z' id='path4544' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><path d='m 2.8222225,291.35554 h 1.4111108 v 0.70552 H 2.8222225 Z' id='path4546' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><path d='m 2.8222225,292.76666 h 1.4111108 v 0.70552 H 2.8222225 Z' id='path4548' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><path d='M 10.301112,293.89583 9.1722226,292.7669' id='path4562' style='fill:none;stroke:#000000;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1'/><path d='m 8.1138888,289.94471 a 1.7638888,1.7638888 0 0 0 -1.7638887,1.76387 1.7638888,1.7638888 0 0 0 1.7638887,1.76389 1.7638888,1.7638888 0 0 0 1.7638892,-1.76389 1.7638888,1.7638888 0 0 0 -1.7638892,-1.76387 z m 0,0.70552 a 1.0583334,1.0583334 0 0 1 1.0583336,1.05835 1.0583334,1.0583334 0 0 1 -1.0583336,1.05832 1.0583334,1.0583334 0 0 1 -1.0583332,-1.05832 1.0583334,1.0583334 0 0 1 1.0583332,-1.05835 z' id='path4564' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1'/><rect height='0.70555556' id='rect4599' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:0.37647059;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1' width='0.70555556' x='4.2333331' y='287.1221'/><rect height='0.70555556' id='rect4601' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:0.37647059;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1' width='0.70555556' x='4.2333331' y='288.53323'/><rect height='0.70555556' id='rect4605' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:0.37647059;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1' width='0.70555556' x='4.2333331' y='289.94437'/><rect height='0.70555556' id='rect4607' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:0.37647059;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1' width='0.70555556' x='4.2333331' y='291.3555'/><rect height='0.70555556' id='rect4609' style='opacity:1;vector-effect:none;fill:#000000;fill-opacity:0.37647059;stroke:none;stroke-width:0.70555556px;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1' width='0.70555556' x='4.2333331' y='292.76663'/></g></svg>\"}}}\n" + "\t";
				if(logLineCount!=0){
					dataMap = getLogLineNumberSpecified(responseData, logLineCount);
				}else {
					dataMap = configMap;
				}
			}else{
            	responseLink = SERVICES_REGISTRY_START_PARENT + pathToData;
				String responseInfo = readNode(responseLink);
				dataMap = getResponseInJson(responseInfo);
			}
			dataMap = formatIfMetricsData(dataMap, pathToData, responseLink);
		} catch (Exception e) {
			LOG.error("Error on query for node information "+ pathToData);
			LOG.error("Error", e);
		}
        LOG.debug("Response map size: "+ dataMap.size());
//		LOG.info("The data from zkNode: "+dataMap);
		return dataMap;
	}

	/**
	 * A method to return only the specified number of service log lines from response.
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
			if(logLineCount<serviceLog.size()) {
				slicedLog = serviceLog.subList(serviceLog.size()-logLineCount,serviceLog.size());
				responseMap.put("Service log", slicedLog);
			}else{
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
	 * @param pathToData
	 * @return
	 */
	public String getTheTokenFromZooKeeper(String pathToData, String tokenNode){
		pathToData = SERVICES_REGISTRY_START_PARENT + pathToData;
		String[] nodeParts = pathToData.split("/");
		if(nodeParts.length == AGENT_DEPTH){
			String nodePath =  pathToData + "/" + tokenNode;
			return readNode(nodePath);
		}
		else if(nodeParts.length>AGENT_DEPTH){
			String nodePath =  RequestPath.getPathOfSpecifiedLength(pathToData, AGENT_DEPTH+1) +  tokenNode;
			return readNode(nodePath);
		}
		else{
			return "";
		}
	}

	/**
	 * Check if the data got is from metrics and if true format according to the method inside ServiceData class
	 * @param dataMap
	 * @param pathToData
	 * @param responseLink
	 * @return
	 */
	private HashMap formatIfMetricsData(HashMap dataMap, String pathToData, String responseLink){
		HashMap tempMap = dataMap;
		if(getAddressEnding(pathToData).equals(ACTIVE_STREAMS_REGISTRY_NODE)){
			for (Object serviceName : dataMap.keySet() ){
				tempMap.put(serviceName, getMetricsWithFormattingStreams(dataMap.get(serviceName), serviceName.toString()));
			}
		}else {
			if (checkIfMetricsData(dataMap)) {
				String serviceName = getStreamName(dataMap);
				tempMap.put("data", getMetricsWithFormatting(dataMap, serviceName));
			}
			tempMap.put("childrenNodes", getListOfChildNodes(SERVICES_REGISTRY_START_PARENT + pathToData));
			tempMap.put("Response link", responseLink);
		}
		//LOG.info("data from data [}", tempMap);
		return tempMap;
	}

	/**
	 * Check if simple node or request node, if request node then needs specific handling.
	 * @param pathToData
	 * @return
	 */
	private HashMap doChecksForSpecialNeedsNodes(String pathToData){
	    String responseLink = "";
	    HashMap respone = new HashMap();
	    boolean actionCall = false;
        String tempPathToNode = SERVICES_REGISTRY_START_PARENT + pathToData;
        if (checkIfNeededURLDownload(pathToData, "downloadables")) {
			tempPathToNode = RequestPath.getPathOfSpecifiedLength(tempPathToNode, 8);
			tempPathToNode = tempPathToNode.substring(0, tempPathToNode.length()-1);
			responseLink = readNode(tempPathToNode);
			responseLink = responseLink + "/" + getAddressEnding(pathToData);
			respone.put("responseLink", responseLink);
        }else  if (checkIfNeededURLControls(pathToData, "_stop") || checkIfNeededURLControls(pathToData, "_start")){
			actionCall = true;
			responseLink = readNode(tempPathToNode);
		} else {
			actionCall = false;
            responseLink = readNode(tempPathToNode);
        }

        if(actionCall){
			actionCall = false;
			String token = getTheTokenFromZooKeeper(pathToData, AUTH_NODE_PATH_ACTION_RIGHTS);
			token =TOKEN_TYPE + " "+token;
			LOG.info("Action token from ZooKeeper"+ token);
			respone.put("token", token);
		}
		respone.put("responseLink", responseLink);
        LOG.info("Response link that makes the call to ZooKeeper REST "+ responseLink);
        return respone;
    }

	/**
	 * Returns the last element in string path
	 * @param path
	 * @return
	 */
	public String getAddressEnding(String path) {
		String pathEnding = path.substring(path.lastIndexOf('/') + 1);
		return pathEnding;
	}

	/**
	 * Returns a list of children nodes to be added to teh main response map
	 * @param parentPath
	 * @return
	 */
	private List<String> getListOfChildNodes (String parentPath){
		Collection<String> nodeNames = null;
		List<String> neededNames = new ArrayList<>();
//		LOG.info("Trying to get the children node list from provided node parent : "+ parentPath);
		ServiceDiscovery<String> serviceDiscovery = null;
		try {

			Stat statResponse = client.checkExists().forPath(parentPath);
			if (statResponse == null) {
				LOG.info("No node exists for path: {} Check the call URL or ZooKeeper node tree "+ parentPath);
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
		} catch (Exception e) {
			LOG.error("Error on getting the children of parent nodes"+ e);
		} finally {
			try {
				serviceDiscovery.close();
			} catch (Exception ignored) {
			}
		}
		return neededNames;
	}


//	private static void checkIfRequestResponseNodeExist(String pathToRequests, String pathToResponses) {
//		try {
//			System.out.println("Checking for response node existence");
//			Stat statResponse = client.checkExists().forPath(pathToResponses);
//
//			if (statResponse == null) {
//				System.out.println("Creating node for responses: " + pathToResponses);
//				CuratorUtils.createNode(pathToResponses, client);
//			}
//			statResponse = client.checkExists().forPath(pathToResponses);
//			if (statResponse != null) {
//				System.out.println("Checking for request node existence");
//				Stat statRequest = client.checkExists().forPath(pathToRequests);
//				if (statRequest == null) {
//					System.out.println("Creating node for requests: " + pathToRequests);
//					CuratorUtils.createNode(pathToRequests, client);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private static String distributedQueueResponse(SimpleDistributedQueue dataResponse) {
//		String responseData = "";
//		try {
//			if (dataResponse.peek() != null) {
//				byte[] dataControl = dataResponse.take();
//				responseData = new String(dataControl);
//				LOG.info("Response message {} "+ responseData);
//			} else {
//				// LOG.info("No response messages found {} "+ responseData);
//			}
//		} catch (Exception e) {
//			LOG.error("Problem on reading queue response");
//			e.printStackTrace();
//		}
//		return responseData;
//	}
//	private String checkAndCallOnRequestDataNoRespone(String pathToData, int waitTime) {
//		String tempPathToNode = SERVICES_REGISTRY_START_PARENT + pathToData;
//		HashMap paramMap = new HashMap();
//		LOG.info("Path to node full :"+ tempPathToNode);
//		if (checkIfNeededURL(pathToData, "threadDump")) {
//			paramMap.put("responsePath"+ tempPathToNode);
//			sendRequestAndWaitForResponseData(tempPathToNode, "getThreadDump"+ paramMap, waitTime);
//		} else if (checkIfNeededURLDownload(pathToData, "downloadables")) {
//			String pathToResponseNode = RequestPath.getPathOfSpecifiedLength(tempPathToNode, 8);
//			String[] arrayUrl = pathToData.split("/");
//			String fileName = arrayUrl[arrayUrl.length - 1];
//			paramMap.put("responsePath"+ pathToResponseNode);
//			paramMap.put("fileName"+ fileName);
//			LOG.info("File that we want to download: "+ fileName);
//			sendRequestAndWaitForResponseData(pathToResponseNode, "getDownloadableContent"+ paramMap, waitTime);
//			tempPathToNode = removeLastChar(RequestPath.getPathOfSpecifiedLength(tempPathToNode, 8));
//		}
//		return tempPathToNode;
//	}

//	/**
//	 * Method that finishes building the path to ZooKeeper node and returns the dataReading inside the node.
//	 *
//	 * @param pathToData
//	 *            The path got on request from front-end or API user to get the dataReading from ZooKeeper node
//	 * @return
//	 */
//	public HashMap getServiceNodeData(String pathToData) {
//		String notJson = "";
//		String responseData = "";
//		HashMap dataMap = new HashMap<>();
//		try {
//			String tempPathToNode = checkAndCallOnRequestDataNoRespone(pathToData, 4);
//			byte[] serviceConfigurationFromZooKeeper = client.getData().watched().forPath(tempPathToNode);
//			responseData = new String(serviceConfigurationFromZooKeeper);
//			if (JsonRpc.isJSONValid(responseData)) {
//				ObjectMapper objectMapper = new ObjectMapper();
//				dataMap = objectMapper.readValue(serviceConfigurationFromZooKeeper, HashMap.class);
//				if (checkIfMetricsData(dataMap)) {
//					String serviceName = getStreamName(dataMap);
//					dataMap.put("data"+ getMetricsWithFormatting(dataMap, serviceName));
//				}
//			} else {
//				notJson = new String(serviceConfigurationFromZooKeeper);
//				dataMap.put("data"+ notJson);
//				// dataMap.put("dataReading"+ tempMap);
//				LOG.info("Not JSON dataReading: "+ notJson);
//			}
//
//			//dataMap.put("childrenNodes"+ getListOfChildNodesWithChildren(tempPathToNode));
//
//			LOG.info("Map is: "+ dataMap);
//
//		} catch (Exception e) {
//			LOG.error("Error on query for node information"+ e);
//		}
//		return dataMap;
//	}
//	/**
//	 * Method that returns a map of node children names together with the boolean value if those children have children
//	 * node of their own.
//	 *
//	 * @param pathToParentNode
//	 *            The path to current node that will be parent to the found children nodes
//	 * @return
//	 */
//	private HashMap getListOfChildNodesWithChildren(String pathToParentNode, HashMap dataMap) {
//		HashMap parentNodesNames = new HashMap<>();
//		LOG.info("Trying to get the children node list from provided node parent : "+ pathToParentNode);
//		ServiceDiscovery<String> serviceDiscovery = null;
//		try {
//			Stat statResponse = client.checkExists().forPath(pathToParentNode);
//			if (statResponse == null) {
//				LOG.info("No node exists for path: {} Check the call URL or ZooKeeper node tree "+ pathToParentNode);
//			} else {
//				serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client)
//						.basePath(pathToParentNode).build();
//				serviceDiscovery.start();
//				Collection<String> serviceNames = serviceDiscovery.queryForNames();
//				LOG.info("List of discovered children nodes: {} "+ serviceNames);
//				for (String nodeChildName : serviceNames) {
//					String pathToNextTreeLvl = pathToParentNode + "/" + nodeChildName;
//					if (getIfPropertyExists(pathToNextTreeLvl, dataMap)) {
//						serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client)
//								.basePath(pathToNextTreeLvl).build();
//						serviceDiscovery.start();
//						Collection<String> serviceChildrenNames = serviceDiscovery.queryForNames();
//						if (serviceChildrenNames.size() > 0) {
//							parentNodesNames.put(nodeChildName, true);
//						} else {
//							parentNodesNames.put(nodeChildName, false);
//						}
//					}
//				}
//				LOG.info("List of parents in children nodes: {} "+ parentNodesNames);
//			}
//		} catch (Exception e) {
//			LOG.error("Error on getting the children of parent nodes"+ e);
//		} finally {
//			try {
//				serviceDiscovery.close();
//			} catch (Exception ignored) {
//			}
//		}
//		return parentNodesNames;
//	}
//
//	/**
//	 * Method that checks if the node selected is a stream, agent or a cluster then returns true.
//	 *
//	 * @param pathToNextTreeLvl
//	 * @return
//	 */
//	private boolean getIfPropertyExists(String pathToNextTreeLvl, HashMap dataMap) {
//		ObjectMapper objectMapper = new ObjectMapper();
//		String responseData = "";
//		try {
//			LOG.info("Checking if property exists in: "+ pathToNextTreeLvl);
//			byte[] serviceConfigurationFromZooKeeper = client.getData().watched().forPath(pathToNextTreeLvl);
//			if (serviceConfigurationFromZooKeeper.length > 0) {
//				responseData = new String(serviceConfigurationFromZooKeeper);
//				if (JsonRpc.isJSONValid(responseData)) {
//					dataMap = objectMapper.readValue(serviceConfigurationFromZooKeeper, HashMap.class);
//				}
//				HashMap configData = (HashMap) dataMap.get("config");
//				if (configData != null) {
//					if (configData.get("streamsIcon") != null
//							|| configData.get("componentLoad").toString().equals("cluster")) {
//						return true;
//					}
//				}
//			}
//		} catch (Exception e1) {
//			LOG.error("Error on getting property exists"+ e1);
//			return false;
//		}
//		return false;
//	}

//    private void sendRequestData(String pathToResponse, String methodName, HashMap requestParameters) {
//        String requestId = UUID.randomUUID().toString();
//        String jsonParams = " ";
//        String pathToRequests = RequestPath.getPathToRequestNode(pathToResponse);
//        LOG.info("Path to response node: "+ pathToResponse);
//        try {
//            deleteAllNodeChildren(pathToRequests);
//            deleteAllNodeChildren(pathToResponse);
//            requestParameters.put("responsePath"+ pathToResponse);
//            jsonParams = new ObjectMapper().writeValueAsString(requestParameters);
//        } catch (Exception e) {
//            LOG.error("Problem converting parameters map to json");
//            e.printStackTrace();
//        }
//        String requestJson = "{\"jsonrpc\":\"2.0\"+\"method\":\"" + methodName + "\"+\"params\":" + jsonParams
//                + "+\"id\":\"" + requestId + "\"}";
//
//        try {
//            checkIfRequestResponseNodeExist(pathToRequests, pathToResponse);
//            SimpleDistributedQueue data = new SimpleDistributedQueue(client, pathToRequests);
//            if (data.offer(requestJson.getBytes())) {
//                LOG.info("Writing request to node successful "+ requestJson);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//	private void sendRequestAndWaitForResponseData(String pathToResponse, String methodName,
//			HashMap requestParameters, int waitTime) {
//		AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);
//
//		String requestId = UUID.randomUUID().toString();
//		AtomicReference<String> responseData = new AtomicReference<>("");
//		String jsonParams = " ";
//		String pathToRequests = RequestPath.getPathToRequestNode(pathToResponse);
//		LOG.info("Path to response node: "+ pathToResponse);
//		try {
//			deleteAllNodeChildren(pathToRequests);
//			deleteAllNodeChildren(pathToResponse);
//			requestParameters.put("responsePath"+ pathToResponse);
//			jsonParams = new ObjectMapper().writeValueAsString(requestParameters);
//		} catch (Exception e) {
//			LOG.error("Problem converting parameters map to json");
//			e.printStackTrace();
//		}
//		String requestJson = "{\"jsonrpc\":\"2.0\"+\"method\":\"" + methodName + "\"+\"params\":" + jsonParams
//				+ "+\"id\":\"" + requestId + "\"}";
//
//		try {
//			checkIfRequestResponseNodeExist(pathToRequests, pathToResponse);
//			SimpleDistributedQueue data = new SimpleDistributedQueue(client, pathToRequests);
//			if (data.offer(requestJson.getBytes())) {
//				LOG.info("Writing request to node successful "+ requestJson);
//			}
//			try {
//				LOG.info("Waiting for {} response"+ methodName);
//				async.with(WatchMode.successOnly).watched().getData().forPath(pathToResponse).event()
//						.thenAccept(event -> {
//							LOG.info("ET:" + event.getType());
//							LOG.info("E:" + event);
//						}).toCompletableFuture().get(waitTime, TimeUnit.SECONDS);
//
//			} catch (Exception e) {
//				LOG.error("Problem on reading response from "+ methodName);
//				e.printStackTrace();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	/**
//	 * Method that finishes building the path to ZooKeeper node and returns the dataReading inside the node.
//	 *
//	 * @param pathToData
//	 *            The path got on request from front-end or API user to get the dataReading from ZooKeeper node
//	 * @return
//	 */
//	public HashMap sendControlRequest(String pathToData, String methodName) {
//		List<String> blockList = new LinkedList<>();
//		String response = "";
//		HashMap parameterMap = new HashMap<>();
//		HashMap nodeResponse = new HashMap<>();
//		try {
//			LOG.info("Got from URL call:"+ pathToData);
//			String tempPathToNode = SERVICES_REGISTRY_START_PARENT + pathToData;
//			blockList = getBlockListIfExists(tempPathToNode);
//			parameterMap.put("items"+ blockList);
//			parameterMap.put("streamName"+ RequestPath.getStreamNameFromPath(tempPathToNode));
//			if (blockList.size() > 0) {
//				if (!blockList.toString().equals("[undefined]") && !blockList.toString().equals("[null]")) {
//					response = sendRequestAndWaitForResponseStreamControls(tempPathToNode, methodName, parameterMap);
//					if (response.length() > 0) {
//						nodeResponse.put("success"+ response);
//					} else {
//						// LOG.error("No response got from the request");
//						nodeResponse.put("Error"+ "No response got from the request for method" + methodName.toString()
//								+ " " + blockList);
//					}
//				} else {
//					LOG.error("No block number has been defined");
//					nodeResponse.put("Error"+ "No block number has been defined");
//				}
//			} else {
//				response = sendRequestAndWaitForResponseStreamControls(tempPathToNode, methodName, parameterMap);
//			}
//			LOG.info("Parameter map is: "+ parameterMap);
//		} catch (Exception e) {
//			LOG.error("Error on query for node information"+ e);
//		}
//		LOG.info("Response is: "+ nodeResponse);
//		return nodeResponse;
//	}
//
//	private List<String> getBlockListIfExists(String tempPathToNode) {
//		List<String> blockList = new LinkedList<>();
//		String requestBlocks = getAddressEnding(tempPathToNode);
//		String[] array = requestBlocks.split("\\.");
//		if (array.length == 0) {
//			blockList.add("Error: No block selected for replay");
//			return blockList;
//		} else {
//			for (String blockNumber : array) {
//				blockList.add(blockNumber);
//			}
//		}
//		return blockList;
//	}
//
//	private String sendRequestAndWaitForResponseStreamControls(String pathToResponse, String methodName,
//			HashMap requestParameters) {
//		String requestId = UUID.randomUUID().toString();
//		AtomicReference<String> responseData = new AtomicReference<>("");
//		String jsonParams = " ";
//		String pathToRequests = RequestPath.getPathToRequestNode(pathToResponse);
//		LOG.info("Path to response node: "+ pathToResponse);
//		try {
//			pathToResponse = RequestPath.getPathToResponseNode(pathToResponse);
//			deleteAllNodeChildren(pathToRequests);
//			deleteAllNodeChildren(pathToResponse);
//			requestParameters.put("responsePath"+ pathToResponse);
//			jsonParams = new ObjectMapper().writeValueAsString(requestParameters);
//		} catch (Exception e) {
//			LOG.error("Problem converting parameters map to json");
//			e.printStackTrace();
//		}
//		String requestJson = "{\"jsonrpc\":\"2.0\"+\"method\":\"" + methodName + "\"+\"params\":" + jsonParams
//				+ "+\"id\":\"" + requestId + "\"}";
//		try {
//			checkIfRequestResponseNodeExist(pathToRequests, pathToResponse);
//			SimpleDistributedQueue dataRequest = new SimpleDistributedQueue(client, pathToRequests);
//			if (dataRequest.offer(requestJson.getBytes())) {
//				LOG.info("Writing request to node successful "+ requestJson);
//			}
//			LOG.info("Waiting for {} response"+ methodName);
//			// SimpleDistributedQueue dataResponse = new SimpleDistributedQueue(client, pathToResponse);
//			// LOG.info("State "+client.getState());
//			// if(!client.getState().equals("STARTED")) {
//			// PathCacheManagerSingleton.Init(client, pathToResponse, false);
//			// }
//			// Thread.sleep(500);
//			// PathCacheManagerSingleton.getPathCacheManager().addListenerToPath(new PathChildrenCacheListener() {
//			// @Override
//			// public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
//			// switch (event.getType()) {
//			// case CHILD_ADDED:
//			// LOG.info("RESPONSE");
//			// responseData.set(distributedQueueResponse(dataResponse));
//			// LOG.info("Received response "+ responseData);
//			// PathCacheManagerSingleton.getPathCacheManager().clear();
//			// break;
//			// default:
//			// break;
//			// }
//			// }
//			// });
//			// Thread.sleep(100);
//		} catch (Throwable e) {
//			LOG.error("Problem on reading response from "+ methodName);
//			e.printStackTrace();
//		}
//
//		return responseData.get();
//	}
//
//	private void deleteAllNodeChildren(String pathToNode) {
//		try {
//			LOG.info("Deleting child nodes of "+ pathToNode);
//			ServiceDiscovery<String> serviceDiscovery = null;
//			serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client).basePath(pathToNode)
//					.build();
//			serviceDiscovery.start();
//			Collection<String> serviceNames = serviceDiscovery.queryForNames();
//			for (String nodeChildName : serviceNames) {
//				String path = pathToNode + "/" + nodeChildName;
//				LOG.info("Child node to delete "+ path);
//				CuratorUtils.deleteNode(path, client);
//			}
//		} catch (Exception e) {
//			LOG.error("Problem on deleting child nodes");
//			e.printStackTrace();
//		}
//	}

}
