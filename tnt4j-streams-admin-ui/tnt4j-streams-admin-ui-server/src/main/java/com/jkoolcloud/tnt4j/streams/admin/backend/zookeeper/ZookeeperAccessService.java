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

import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.tnt4j.streams.admin.backend.ServiceData;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.HttpUtils;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.RequestPath;
import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.utils.JsonRpc;

/**
 * The type Zookeeper access service for getting ZooKeeper dataReading to return to API.
 */
@Singleton
public class ZookeeperAccessService {
	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperAccessService.class);

	private static String SERVICES_REGISTRY_START_NODE;
	private static String SERVICES_REGISTRY_START_PARENT;
	private static String SERVICE_DOWNLOAD_PATH;
    private static String ACTIVE_STREAMS_REGISTRY_NODE;

	private static CuratorFramework client;

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
		ZookeeperAccessService.init();

		// zooAccess.getTreeNodes();
		// zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream2");
		// zooAccess.getListOfChildNodesWithChildren("/streams/v1/clusters/clusterBlockchainMainnets/streamsAgentEth");
		// zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/threadDump");
		// zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth");
		// zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/downloadables/streamAdminLogger.log");
		// zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/downloadables/streamAdminLogger.log");
		// Boolean data = JsonRpc.isJSONValid("{ \"developers\": [{ \"firstName\":\"Linus\" , \"lastName\":\"Torvalds\"
		// }, " +
		// "{ \"firstName\":\"John\" , \"lastName\":\"von Neumann\" } ]}");
		// LOG.info(data.toString());
		// zooAccess.sendControlRequest(
		// "/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream2/incomplete/8",
		// "replayBlocks");
       //zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/downloadables/tnt4j-streams-activities.log");
        //zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth");
		//zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/sampleConfigurations");
		zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/_streamsAndMetrics");
		//zooAccess.getServiceNodeInfoFromLinkForReplay("/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream2/4441");

		zooAccess.destroy();
	}

	/**
	 * Init ZooKeeper connection.
	 */
	@PostConstruct
	private static void init() {
		try {
			SERVICES_REGISTRY_START_NODE = PropertyData.getProperty("serviceRegistryStartNode");
			SERVICES_REGISTRY_START_PARENT = PropertyData.getProperty("serviceRegistryStartNodeParent");
			SERVICE_DOWNLOAD_PATH = PropertyData.getProperty("pathToDownloadsNeeded");
            ACTIVE_STREAMS_REGISTRY_NODE = PropertyData.getProperty("activeStreamRegistry");

			String ZOOKEEPER_URL = PropertyData.getProperty("ZooKeeperAddress");
			LOG.info("Connecting to Zookeeper at {}", ZOOKEEPER_URL);
			client = CuratorFrameworkFactory.newClient(ZOOKEEPER_URL, new ExponentialBackoffRetry(1000, 3));
			client.start();
			LOG.info("Connected to Zookeeper successfully at {}", ZOOKEEPER_URL);
		} catch (Exception e) {
			LOG.error("Error on Zookeeper connection start", e);
		}
	}

	/**
	 * Read data from the provided node
	 *
	 * @param nodePath
	 * @return
	 */
	private static String readNode(String nodePath) {
		String responseData = "";
		try {
			byte[] nodeLinkBytes = client.getData().watched().forPath(nodePath);
			responseData = new String(nodeLinkBytes);
			LOG.info("Response data: {}", responseData);
		} catch (Exception e) {
			LOG.error("Error on query for node information {}", nodePath);
			LOG.error("Error", e);
		}
		return responseData;
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
			LOG.error("Error", e);
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
			LOG.info("Formatted metrics dataReading {}", tempMap);

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
	private static Map getMetricsWithFormatting(HashMap dataMap, String serviceName) {
		Map tempMap = null;
		try {
			tempMap = ServiceData.parseJsonDataIntoSimpleFormatZooKeeper(dataMap, serviceName);
			LOG.info("Formatted metrics dataReading {}", tempMap);

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
			LOG.info("DATA match {}", idStr);
			if (idStr.equals(neededName)) {
				return true;
			}
		} catch (Exception e) {
			LOG.error("There was a problem while checking if the call is from {} node {} ", pathToData, e);
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
			LOG.error("There was a problem while checking if the call is from {} node {} ", pathToData, e);
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
	private void destroy() {
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
		LOG.info("ZooKeeper Node Tree = {}", nodeMap);
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
		ServiceDiscovery<String> serviceDiscovery = null;
		try {
			String nodeName = getAddressEnding(path);
			Stat statResponse = client.checkExists().forPath(path);
			if (statResponse == null) {
				LOG.info("No call path exists: {} Check the configuration file ", path);
			}
			else if(nodeName.charAt(0)=='_'){
				LOG.info("The node {} does not need to be shown in tree view ", path);
			}else {
				Map<String, Integer> tempNode = new HashMap<>();
				// tempNode.put(path, nodeLevel);
				// String json = new ObjectMapper().writeValueAsString(tempNode);
				myNodeMap.put(path, parentNode);
				parentNode = path;
				nodeLevel++;
				serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client).basePath(path).build();
				serviceDiscovery.start();
				Collection<String> serviceNames = serviceDiscovery.queryForNames();
				for (String serviceName : serviceNames) {
					try {
						String pathToNextTreeLvl = path + "/" + serviceName;
						getZooKeeperTreeNodes(pathToNextTreeLvl, myNodeMap, parentNode, nodeLevel);
					} catch (Exception e1) {
						destroy();
						LOG.error("Error on tree branch creation", e1);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Error on tree creation", e);
		} finally {
			try {
				Objects.requireNonNull(serviceDiscovery).close();
			} catch (Exception ignored) {
			}
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
			pathToNode = prepareReplayLink(pathToData);// doChecksForSpecialNeedsNodes(pathToData);
			blocksToReplay = getAddressEnding(pathToData);
			responseLink = readNode(pathToNode) + blocksToReplay;
			responseData = HttpUtils.readURLData(responseLink);
			LOG.info("Response data {}", responseData);
			dataMap = getResponseInJson(responseData);
			dataMap.put("childrenNodes", getListOfChildNodes(SERVICES_REGISTRY_START_PARENT + pathToData));
			dataMap.put("Response link", responseLink);

		} catch (Exception e) {
			LOG.error("Error on query for node information {}", pathToData);
			LOG.error("Error", e);
		}
		LOG.debug("Response map for debuging {}", dataMap);
		return dataMap;
	}

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
	public HashMap getServiceNodeInfoFromLink(String pathToData) {
		String responseLink, responseData;
		HashMap dataMap = new HashMap();
		try {
            responseLink = doChecksForSpecialNeedsNodes(pathToData);
			responseData = HttpUtils.readURLData(responseLink);
            LOG.info("Response data {}", responseData);
			dataMap = getResponseInJson(responseData);
			if(getAddressEnding(pathToData).equals(ACTIVE_STREAMS_REGISTRY_NODE)){
				for (Object serviceName : dataMap.keySet() ){
					dataMap.put(serviceName, getMetricsWithFormattingStreams(dataMap.get(serviceName), serviceName.toString()));
				}
			}else {
				if (checkIfMetricsData(dataMap)) {
					String serviceName = getStreamName(dataMap);
					dataMap.put("data", getMetricsWithFormatting(dataMap, serviceName));
				}

				dataMap.put("childrenNodes", getListOfChildNodes(SERVICES_REGISTRY_START_PARENT + pathToData));
				dataMap.put("Response link", responseLink);
			}
		} catch (Exception e) {
			LOG.error("Error on query for node information {}", pathToData);
			LOG.error("Error", e);
		}
        LOG.debug("Response map for debuging {}", dataMap);
		return dataMap;
	}

	private String doChecksForSpecialNeedsNodes(String pathToData){
	    String responseLink = "";
        String tempPathToNode = SERVICES_REGISTRY_START_PARENT + pathToData;
        if (checkIfNeededURLDownload(pathToData, "downloadables")) {
            tempPathToNode = RequestPath.getPathOfSpecifiedLength(tempPathToNode, 8) + SERVICE_DOWNLOAD_PATH;
            responseLink = readNode(tempPathToNode);
			responseLink = responseLink+ "/" + getAddressEnding(pathToData);
        }//else if(getAddressEnding(pathToData).equals(ACTIVE_STREAMS_REGISTRY_NODE)){
            //responseLink = readNode(tempPathToNode);
        else if (checkIfNeededURLDownload(pathToData, "replayBlock")){
			responseLink = readNode(tempPathToNode);
        }else  if (checkIfNeededURLDownload(pathToData, "stop")){
			responseLink = readNode(tempPathToNode);
		}else  if (checkIfNeededURLDownload(pathToData, "start")){
			responseLink = readNode(tempPathToNode);
		}else  if (checkIfNeededURLDownload(pathToData, "pause")){
			responseLink = readNode(tempPathToNode);
		}else  if (checkIfNeededURLDownload(pathToData, "resume")){
			responseLink = readNode(tempPathToNode);
		}
        else {
            responseLink = readNode(tempPathToNode);
        }
        LOG.info("Path to node full :{}", tempPathToNode);
        LOG.info("Response link {}", responseLink);
        return responseLink;
    }


	private String getAddressEnding(String path) {
		String pathEnding = path.substring(path.lastIndexOf('/') + 1);
		return pathEnding;
	}

	private List<String> getListOfChildNodes (String parentPath){
		Collection<String> nodeNames = null;
		List<String> neededNames = new ArrayList<>();
		LOG.info("Trying to get the children node list from provided node parent : {}", parentPath);
		ServiceDiscovery<String> serviceDiscovery = null;
		try {

			Stat statResponse = client.checkExists().forPath(parentPath);
			if (statResponse == null) {
				LOG.info("No node exists for path: {} Check the call URL or ZooKeeper node tree ", parentPath);
			} else {
				serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client)
						.basePath(parentPath).build();
				serviceDiscovery.start();
				nodeNames = serviceDiscovery.queryForNames();
				for (String nodeChildName : nodeNames) {
					String nodeName = getAddressEnding(nodeChildName);
					if (nodeName.charAt(0) == '_') {
						LOG.info("The node {} does not need to be shown in tree view ", nodeChildName);
					} else {
						neededNames.add(nodeChildName);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Error on getting the children of parent nodes", e);
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
//				LOG.info("Response message {} ", responseData);
//			} else {
//				// LOG.info("No response messages found {} ", responseData);
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
//		LOG.info("Path to node full :{}", tempPathToNode);
//		if (checkIfNeededURL(pathToData, "threadDump")) {
//			paramMap.put("responsePath", tempPathToNode);
//			sendRequestAndWaitForResponseData(tempPathToNode, "getThreadDump", paramMap, waitTime);
//		} else if (checkIfNeededURLDownload(pathToData, "downloadables")) {
//			String pathToResponseNode = RequestPath.getPathOfSpecifiedLength(tempPathToNode, 8) + SERVICE_DOWNLOAD_PATH;
//			String[] arrayUrl = pathToData.split("/");
//			String fileName = arrayUrl[arrayUrl.length - 1];
//			paramMap.put("responsePath", pathToResponseNode);
//			paramMap.put("fileName", fileName);
//			LOG.info("File that we want to download: {}", fileName);
//			sendRequestAndWaitForResponseData(pathToResponseNode, "getDownloadableContent", paramMap, waitTime);
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
//					dataMap.put("data", getMetricsWithFormatting(dataMap, serviceName));
//				}
//			} else {
//				notJson = new String(serviceConfigurationFromZooKeeper);
//				dataMap.put("data", notJson);
//				// dataMap.put("dataReading", tempMap);
//				LOG.info("Not JSON dataReading: {}", notJson);
//			}
//
//			//dataMap.put("childrenNodes", getListOfChildNodesWithChildren(tempPathToNode));
//
//			LOG.info("Map is: {}", dataMap);
//
//		} catch (Exception e) {
//			LOG.error("Error on query for node information", e);
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
//		LOG.info("Trying to get the children node list from provided node parent : {}", pathToParentNode);
//		ServiceDiscovery<String> serviceDiscovery = null;
//		try {
//			Stat statResponse = client.checkExists().forPath(pathToParentNode);
//			if (statResponse == null) {
//				LOG.info("No node exists for path: {} Check the call URL or ZooKeeper node tree ", pathToParentNode);
//			} else {
//				serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client)
//						.basePath(pathToParentNode).build();
//				serviceDiscovery.start();
//				Collection<String> serviceNames = serviceDiscovery.queryForNames();
//				LOG.info("List of discovered children nodes: {} ", serviceNames);
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
//				LOG.info("List of parents in children nodes: {} ", parentNodesNames);
//			}
//		} catch (Exception e) {
//			LOG.error("Error on getting the children of parent nodes", e);
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
//			LOG.info("Checking if property exists in: {}", pathToNextTreeLvl);
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
//			LOG.error("Error on getting property exists", e1);
//			return false;
//		}
//		return false;
//	}

//    private void sendRequestData(String pathToResponse, String methodName, HashMap requestParameters) {
//        String requestId = UUID.randomUUID().toString();
//        String jsonParams = " ";
//        String pathToRequests = RequestPath.getPathToRequestNode(pathToResponse);
//        LOG.info("Path to response node: {}", pathToResponse);
//        try {
//            deleteAllNodeChildren(pathToRequests);
//            deleteAllNodeChildren(pathToResponse);
//            requestParameters.put("responsePath", pathToResponse);
//            jsonParams = new ObjectMapper().writeValueAsString(requestParameters);
//        } catch (Exception e) {
//            LOG.error("Problem converting parameters map to json");
//            e.printStackTrace();
//        }
//        String requestJson = "{\"jsonrpc\":\"2.0\",\"method\":\"" + methodName + "\",\"params\":" + jsonParams
//                + ",\"id\":\"" + requestId + "\"}";
//
//        try {
//            checkIfRequestResponseNodeExist(pathToRequests, pathToResponse);
//            SimpleDistributedQueue data = new SimpleDistributedQueue(client, pathToRequests);
//            if (data.offer(requestJson.getBytes())) {
//                LOG.info("Writing request to node successful {}", requestJson);
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
//		LOG.info("Path to response node: {}", pathToResponse);
//		try {
//			deleteAllNodeChildren(pathToRequests);
//			deleteAllNodeChildren(pathToResponse);
//			requestParameters.put("responsePath", pathToResponse);
//			jsonParams = new ObjectMapper().writeValueAsString(requestParameters);
//		} catch (Exception e) {
//			LOG.error("Problem converting parameters map to json");
//			e.printStackTrace();
//		}
//		String requestJson = "{\"jsonrpc\":\"2.0\",\"method\":\"" + methodName + "\",\"params\":" + jsonParams
//				+ ",\"id\":\"" + requestId + "\"}";
//
//		try {
//			checkIfRequestResponseNodeExist(pathToRequests, pathToResponse);
//			SimpleDistributedQueue data = new SimpleDistributedQueue(client, pathToRequests);
//			if (data.offer(requestJson.getBytes())) {
//				LOG.info("Writing request to node successful {}", requestJson);
//			}
//			try {
//				LOG.info("Waiting for {} response", methodName);
//				async.with(WatchMode.successOnly).watched().getData().forPath(pathToResponse).event()
//						.thenAccept(event -> {
//							LOG.info("ET:" + event.getType());
//							LOG.info("E:" + event);
//						}).toCompletableFuture().get(waitTime, TimeUnit.SECONDS);
//
//			} catch (Exception e) {
//				LOG.error("Problem on reading response from {}", methodName);
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
//			LOG.info("Got from URL call:{}", pathToData);
//			String tempPathToNode = SERVICES_REGISTRY_START_PARENT + pathToData;
//			blockList = getBlockListIfExists(tempPathToNode);
//			parameterMap.put("items", blockList);
//			parameterMap.put("streamName", RequestPath.getStreamNameFromPath(tempPathToNode));
//			if (blockList.size() > 0) {
//				if (!blockList.toString().equals("[undefined]") && !blockList.toString().equals("[null]")) {
//					response = sendRequestAndWaitForResponseStreamControls(tempPathToNode, methodName, parameterMap);
//					if (response.length() > 0) {
//						nodeResponse.put("success", response);
//					} else {
//						// LOG.error("No response got from the request");
//						nodeResponse.put("Error", "No response got from the request for method" + methodName.toString()
//								+ " " + blockList);
//					}
//				} else {
//					LOG.error("No block number has been defined");
//					nodeResponse.put("Error", "No block number has been defined");
//				}
//			} else {
//				response = sendRequestAndWaitForResponseStreamControls(tempPathToNode, methodName, parameterMap);
//			}
//			LOG.info("Parameter map is: {}", parameterMap);
//		} catch (Exception e) {
//			LOG.error("Error on query for node information", e);
//		}
//		LOG.info("Response is: {}", nodeResponse);
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
//		LOG.info("Path to response node: {}", pathToResponse);
//		try {
//			pathToResponse = RequestPath.getPathToResponseNode(pathToResponse);
//			deleteAllNodeChildren(pathToRequests);
//			deleteAllNodeChildren(pathToResponse);
//			requestParameters.put("responsePath", pathToResponse);
//			jsonParams = new ObjectMapper().writeValueAsString(requestParameters);
//		} catch (Exception e) {
//			LOG.error("Problem converting parameters map to json");
//			e.printStackTrace();
//		}
//		String requestJson = "{\"jsonrpc\":\"2.0\",\"method\":\"" + methodName + "\",\"params\":" + jsonParams
//				+ ",\"id\":\"" + requestId + "\"}";
//		try {
//			checkIfRequestResponseNodeExist(pathToRequests, pathToResponse);
//			SimpleDistributedQueue dataRequest = new SimpleDistributedQueue(client, pathToRequests);
//			if (dataRequest.offer(requestJson.getBytes())) {
//				LOG.info("Writing request to node successful {}", requestJson);
//			}
//			LOG.info("Waiting for {} response", methodName);
//			// SimpleDistributedQueue dataResponse = new SimpleDistributedQueue(client, pathToResponse);
//			// LOG.info("State {}",client.getState());
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
//			// LOG.info("Received response {}", responseData);
//			// PathCacheManagerSingleton.getPathCacheManager().clear();
//			// break;
//			// default:
//			// break;
//			// }
//			// }
//			// });
//			// Thread.sleep(100);
//		} catch (Throwable e) {
//			LOG.error("Problem on reading response from {}", methodName);
//			e.printStackTrace();
//		}
//
//		return responseData.get();
//	}
//
//	private void deleteAllNodeChildren(String pathToNode) {
//		try {
//			LOG.info("Deleting child nodes of {}", pathToNode);
//			ServiceDiscovery<String> serviceDiscovery = null;
//			serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client).basePath(pathToNode)
//					.build();
//			serviceDiscovery.start();
//			Collection<String> serviceNames = serviceDiscovery.queryForNames();
//			for (String nodeChildName : serviceNames) {
//				String path = pathToNode + "/" + nodeChildName;
//				LOG.info("Child node to delete {}", path);
//				CuratorUtils.deleteNode(path, client);
//			}
//		} catch (Exception e) {
//			LOG.error("Problem on deleting child nodes");
//			e.printStackTrace();
//		}
//	}

}
