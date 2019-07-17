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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;

import com.jkoolcloud.tnt4j.streams.admin.backend.ServiceData;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.queue.SimpleDistributedQueue;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.WatchMode;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;
import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.utils.JsonRpc;
import sun.rmi.runtime.Log;

/**
 * The type Zookeeper access service for getting ZooKeeper data to return to API.
 */
@Singleton
public class ZookeeperAccessService {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperAccessService.class);

    private static String SERVICES_REGISTRY_NODE;

    private static String SERVICES_REGISTRY_START_NODE;
	private static String SERVICES_REGISTRY_START_PARENT;
	private static String SERVICE_DOWNLOAD_PATH;

    private static CuratorFramework client;

    /**
     * Init ZooKeeper connection.
     */
    @PostConstruct
    private void init() {
        try {
            SERVICES_REGISTRY_START_NODE = PropertyData.getProperty("serviceRegistryStartNode");
			SERVICES_REGISTRY_START_PARENT = PropertyData.getProperty("serviceRegistryStartNodeParent");
			SERVICE_DOWNLOAD_PATH = PropertyData.getProperty("pathToDownloadsNeeded");


            //SERVICES_REGISTRY_NODE = PropertyData.getProperty("serviceRegistryNode");
            String ZOOKEEPER_URL = PropertyData.getProperty("ZooKeeperUrl");
            LOG.info("Connecting to Zookeeper at {}", ZOOKEEPER_URL);
            client = CuratorFrameworkFactory.newClient(ZOOKEEPER_URL, new ExponentialBackoffRetry(1000, 3));
            client.start();
            LOG.info("Connected to Zookeeper successfully at {}", ZOOKEEPER_URL);
        } catch (Exception e) {
            LOG.error("Error on Zookeeper connection start", e);
        }
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
     * The entry point of application.
     *
     * @param args
     *            the input arguments
     * @throws Exception
     *             the exception
     */
    public static void main(String[] args) throws Exception {
        ZookeeperAccessService zooAccess = new ZookeeperAccessService();

        zooAccess.init();

        // zooAccess.getTreeNodes();
        //zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamAgentBtc/btcToJkool");
       // zooAccess.getListOfChildNodesWithChildren("/streams/v1/clusters/clusterBlockchainMainnets/streamsAgentEth");
//		zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/threadDump");

		//zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/download");

//		zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/downloadables/streamAdminLogger.log");

		//zooAccess.getServiceNodeData("/clusters/clusterBlockchainMainnets/streamsAgentEth/downloadables/streamAdminLogger.log");

		zooAccess.sendControlRequest("/clusters/clusterBlockchainMainnets/streamsAgentBch/BitcoinCashStream/incomplete/590694.444444.666666.88888", "replayBlock");

        zooAccess.destroy();
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
    private Map<String, String> getZooKeeperTreeNodes(String path, Map<String, String> myNodeMap, String parentNode, int nodeLevel) {
        ServiceDiscovery<String> serviceDiscovery = null;
        try {
            Stat statResponse = client.checkExists().forPath(path);
            if (statResponse == null) {
                LOG.info("No call path exists: {} Check the configuration file ", path);
            } else {
                Map<String, Integer> tempNode = new HashMap<>();
                //tempNode.put(path, nodeLevel);
                //String json = new ObjectMapper().writeValueAsString(tempNode);
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
	 * Gets stream services.
	 *
	 * @return the stream services
	 */
	public List<String> getStreamServices() {
		List<String> services = new ArrayList<>();
		try (ServiceDiscovery<String> serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client)
				.basePath(SERVICES_REGISTRY_NODE).build()) {
			serviceDiscovery.start();
			Collection<String> serviceNames = serviceDiscovery.queryForNames();
			LOG.info("Services found:{}", serviceNames);
			for (String serviceName : serviceNames) {
				try {
					LOG.info("SN:{}", serviceName);
					services.add(serviceName);
					Collection<ServiceInstance<String>> instances = serviceDiscovery.queryForInstances(serviceName);
					LOG.info("Instances:{}", instances);
				} catch (Exception e1) {
					LOG.error("Error on query for instances", e1);
				}
			}
		} catch (Exception e) {
			LOG.error("Error on query for names", e);
		}
		return services;
	}

	/**
	 * Method that finishes building the path to ZooKeeper node and returns the data inside the node.
	 * @param pathToData
	 * 				The path got on request from front-end or API user to get the data from ZooKeeper node
	 * @return
	 */
	public HashMap getServiceNodeData(String pathToData) {
		String notJson = "";
		//HashMap tempMap = new HashMap<>();
		HashMap myMap = new HashMap<>();
		try {
			LOG.info("Got from URL call:{}", pathToData);

			String tempPathToNode = SERVICES_REGISTRY_START_PARENT+pathToData;

			LOG.info("Path to node full :{}", tempPathToNode);
			if(checkIfNeededURL(pathToData, "threadDump")){
				sendRequestAndWaitForResponseData(tempPathToNode, "getThreadDump", 10, "");
			}
			else if(checkIfNeededURLDownload(pathToData, "download")){
				String[] arrayUrl = pathToData.split("/");
				int pathLengthRemove = arrayUrl[arrayUrl.length-2].length();
				String tempValue =  tempPathToNode.substring(0, tempPathToNode.length() - arrayUrl[arrayUrl.length-1].length()-1);
				LOG.info("FIle that we want to download: {}", arrayUrl[arrayUrl.length-1]);
				LOG.info("Remove from request end : {}",tempValue);
				sendRequestAndWaitForResponseData(tempValue, "getDownloadableContent", pathLengthRemove, arrayUrl[arrayUrl.length-1]);
				tempPathToNode = tempValue+SERVICE_DOWNLOAD_PATH;
			}
			//			else if(checkIfNeededURL(pathToData, "download")){
//				sendRequestAndWaitForResponseData(myMap, tempPathToNode, "getDownloadableContent", 8);
//			}

			byte[] serviceConfigurationFromZooKeeper = client.getData().watched().forPath(tempPathToNode);

			if (serviceConfigurationFromZooKeeper[0] == '{' || serviceConfigurationFromZooKeeper[1] == '{' || serviceConfigurationFromZooKeeper[0] == '[' || serviceConfigurationFromZooKeeper[1] == '[') {
				ObjectMapper objectMapper = new ObjectMapper();
				myMap = objectMapper.readValue(serviceConfigurationFromZooKeeper, HashMap.class);

				if(checkIfNeededNodeConfig(myMap)) {
					myMap.put("data", getMetricsWithFormatting(myMap));
				}
			}
			else{
				notJson = new String(serviceConfigurationFromZooKeeper);
				myMap.put("data", notJson);
				//myMap.put("data", tempMap);
				LOG.info("Not JSON data: {}", notJson);
			}

			myMap.put("parentsInChildren", getListOfChildNodesWithChildren(tempPathToNode));

			LOG.info("Map is: {}", myMap);

		} catch (Exception e) {
			LOG.error("Error on query for node information", e);
		}
		return myMap;
	}

	private HashMap getListOfChildNodesWithChildren(String pathToParentNode){
		HashMap parentNodesNames = new HashMap<>();
		LOG.info("Trying to get the children node list from provided node parent : {}", pathToParentNode);
		ServiceDiscovery<String> serviceDiscovery = null;
		try {
			Stat statResponse = client.checkExists().forPath(pathToParentNode);
			if (statResponse == null) {
				LOG.info("No node exists for path: {} Check the call URL or ZooKeeper node tree ", pathToParentNode);
			} else {
				serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client).basePath(pathToParentNode).build();
				serviceDiscovery.start();
				Collection<String> serviceNames = serviceDiscovery.queryForNames();
				LOG.info("List of discovered children nodes: {} ", serviceNames);
				for (String nodeChildName : serviceNames) {
					try {
						String pathToNextTreeLvl = pathToParentNode + "/" + nodeChildName;
						if (getIfPropertyExists(pathToNextTreeLvl)) {
							serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(client).basePath(pathToNextTreeLvl).build();
							serviceDiscovery.start();
							Collection<String> serviceChildrenNames = serviceDiscovery.queryForNames();
							if (serviceChildrenNames.size() > 0) {
								parentNodesNames.put(nodeChildName, true);
							}
							else{
								parentNodesNames.put(nodeChildName, false);
							}
						}
					} catch (Exception e1) {
						destroy();
						LOG.error("Error on checking for parents in children nodes", e1);
					}
				}
				LOG.info("List of parents in children nodes: {} ", parentNodesNames);
			}
		} catch (Exception e) {
			LOG.error("Error on getting the children of parent nodes", e);
		} finally {
			try {
				serviceDiscovery.close();
			} catch (Exception ignored) {
			}
		}
		return parentNodesNames;
	}

	private boolean getIfPropertyExists(String pathToNextTreeLvl) {
		ObjectMapper objectMapper = new ObjectMapper();
		HashMap myMap = new HashMap<>();
		try {
			LOG.info("Checking if property exists in: {}", pathToNextTreeLvl);
			byte[] serviceConfigurationFromZooKeeper = client.getData().watched().forPath(pathToNextTreeLvl);
			if (serviceConfigurationFromZooKeeper.length > 0) {
				if (serviceConfigurationFromZooKeeper[0] == '{' || serviceConfigurationFromZooKeeper[1] == '{' || serviceConfigurationFromZooKeeper[0] == '[' || serviceConfigurationFromZooKeeper[1] == '[') {
					myMap = objectMapper.readValue(serviceConfigurationFromZooKeeper, HashMap.class);
				}

				HashMap configData = (HashMap) myMap.get("config");
				if (configData != null) {
					//LOG.info("THE DATA I WANT TO SEE {}",configData.get("componentLoad").toString());
					if (configData.get("streamsIcon") != null || configData.get("componentLoad").toString().equals("cluster")) {
						return true;
					}
				}
			}
		}
		catch (Exception e1) {
			LOG.error("Error on getting property exists", e1);
			return false;
		}
		return false;
	}

	/**
	 * 	Get the map data ant format it using the method prom service data
	 * @param myMap  The data from ZooKeeper node inside Map
	 * @return
	 */
	private Map getMetricsWithFormatting(HashMap myMap){
		Map tempMap = null;
		String serviceName = "";
		try {
			LOG.info("Formatting service metric data {}", myMap);
			if (checkIfNeededNodeConfig(myMap)) {
				serviceName = getStreamName(myMap);
				LOG.info("SERVICE DATA {}", serviceName);

				tempMap = ServiceData.parseJsonDataIntoSimpleFormatZooKeeper(myMap, serviceName);
				LOG.info("Formatted metrics data {}", tempMap);
				LOG.info("Formatted metrics data {}", tempMap);

			}
		}
		catch(Exception e){
			LOG.error("Problem while trying to parse metrics data", e);
		}
		return  tempMap;
	}

	/**
	 * Check if the data call is for metrics data
	 * @param myMap The data from ZooKeeper node inside Map
	 * @return
	 */
	private boolean checkIfNeededNodeConfig(HashMap myMap){
		HashMap configData = (HashMap) myMap.get("config");
		if (configData != null) {
			if (configData.get("componentLoad").toString().equals("metrics") || configData.get("componentLoad").toString().equals("service")) {
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	private boolean checkIfNeededURL(String pathToData, String neededName){
		try {
			String idStr = pathToData.substring(pathToData.lastIndexOf('/') + 1);
			LOG.info("DATA match {}", idStr);
			if (idStr.equals(neededName)) {
				return true;
			}
		}catch (Exception e){
			LOG.error("There was a problem while checking if the call is from {} node {} ",pathToData, e);
		}
		return false;
	}

	private boolean checkIfNeededURLDownload(String pathToData, String neededName) {
		try {
			String[] arrayUrl = pathToData.split("/");
			if (arrayUrl[arrayUrl.length-2].contains(neededName)) {
				return true;
			}
		} catch (Exception e) {
			LOG.error("There was a problem while checking if the call is from {} node {} ",pathToData, e);
		}
		return false;
	}

	private void sendRequestAndWaitForResponseData(String pathToResponse, String methodName, int nodeNameLength, String fileName) {
		AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);
		String requestId = UUID.randomUUID().toString();
		String pathToRequests = pathToResponse.substring(0, pathToResponse.length() - nodeNameLength) + "requests";
		LOG.info("Path To request dump: {}", pathToRequests);
		LOG.info("Path To response dump: {}", pathToResponse);
		String requestJson = "{\"jsonrpc\":\"2.0\",\"method\":\"" + methodName + "\",\"params\":{\"responsePath\" : \"" + pathToResponse + "\"},\"id\":\"" + requestId + "\"}";
		if(!fileName.equals("")){
			LOG.info("File name if Exists: {}", fileName);
			pathToResponse = pathToResponse + SERVICE_DOWNLOAD_PATH;
			requestJson = "{\"jsonrpc\":\"2.0\",\"method\":\"" + methodName + "\",\"params\":{\"responsePath\" : \"" + pathToResponse + "\", \"fileName\" : \"" + fileName + "\"},\"id\":\"" + requestId + "\"}";
		}
		try {
			checkIfRequestResponseNodeExist(pathToRequests, pathToResponse);
			SimpleDistributedQueue data = new SimpleDistributedQueue(client, pathToRequests);
			if (data.offer(requestJson.getBytes())) {
				LOG.info("Writing request to node successful {}", requestJson);
			}
			try {
				LOG.info("Waiting for {} response", methodName);
				async.with(WatchMode.successOnly).watched().getData().forPath(pathToResponse).event()
						.thenAccept(event -> {
							LOG.info("ET:" + event.getType());
							LOG.info("E:" + event);
						}).toCompletableFuture().get(4, TimeUnit.SECONDS);

			} catch (Exception e) {
				LOG.error("Problem on reading response from {}", methodName);
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createNode(String path) {
		try {
			String result = client.create().forPath(path);
			System.out.println("Node created:" + result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteNode(String path) {
		try {
			client.delete().forPath(path);
		} catch (Exception e) {
			LOG.error("Node with path: " + path + " was not deleted successfully");
			e.printStackTrace();
		}
	}

	private void checkIfRequestResponseNodeExist(String pathToRequests, String pathToResponses) {
		try {
			System.out.println("Checking for response node existence");
			Stat statResponse = client.checkExists().forPath(pathToResponses);

			if (statResponse == null) {
				System.out.println("Creating node for responses: " + pathToResponses);
				createNode(pathToResponses);
			}
			statResponse = client.checkExists().forPath(pathToResponses);
			if (statResponse != null) {
				System.out.println("Checking for request node existence");
				Stat statRequest = client.checkExists().forPath(pathToRequests);
				if (statRequest == null) {
					System.out.println("Creating node for requests: " + pathToRequests);
					createNode(pathToRequests);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get service name from inside the metrics information
	 * @param myMap The data from ZooKeeper node inside Map
	 * @return
	 */
	private String getStreamName(HashMap myMap){
		HashMap serviceData = (HashMap) myMap.get("data");
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

	private String getAddressEnding(String path){
		String pathEnding = path.substring(path.lastIndexOf('/') + 1);
		return pathEnding;
	}



	/**
	 * Method that finishes building the path to ZooKeeper node and returns the data inside the node.
	 * @param pathToData
	 * 				The path got on request from front-end or API user to get the data from ZooKeeper node
	 * @return
	 */
	public HashMap sendControlRequest(String pathToData, String methodName) {
		List<String> blockList = new LinkedList<>();
		HashMap parameterMap = new HashMap<>();
		HashMap nodeResponse = new HashMap<>();
		try {
			LOG.info("Got from URL call:{}", pathToData);
			String tempPathToNode = SERVICES_REGISTRY_START_PARENT+pathToData;
			blockList = getBlockListIfExists(tempPathToNode);
			parameterMap.put("Items", blockList);
			if(blockList.size()>0) {
				sendRequestAndWaitForResponseStreamControls(tempPathToNode, methodName, parameterMap);

				byte[] serviceConfigurationFromZooKeeper = client.getData().watched().forPath(tempPathToNode);
				if (serviceConfigurationFromZooKeeper[0] == '{' || serviceConfigurationFromZooKeeper[1] == '{' || serviceConfigurationFromZooKeeper[0] == '[' || serviceConfigurationFromZooKeeper[1] == '[') {
					ObjectMapper objectMapper = new ObjectMapper();
					nodeResponse = objectMapper.readValue(serviceConfigurationFromZooKeeper, HashMap.class);
				} else {
					String data = new String(serviceConfigurationFromZooKeeper);
					nodeResponse.put(methodName, data);
				}
			}
			else{
				nodeResponse.put("Error", "No blocks specified in request");
			}
			LOG.info("Response is: {}", parameterMap);
		} catch (Exception e) {
			LOG.error("Error on query for node information", e);
		}
		return nodeResponse;
	}

	private List<String> getBlockListIfExists(String tempPathToNode){
		List<String> blockList = new LinkedList<>();
		String requestBlocks = getAddressEnding(tempPathToNode);
		String[] array = requestBlocks.split("\\.");
		if(array.length==0){
			blockList.add("Error: No block selected for replay");
			return blockList;
		}
		else {
			for (String blockNumber : array) {
				blockList.add(blockNumber);
			}
		}
		return blockList;
	}



	private String sendRequestAndWaitForResponseStreamControls(String pathToResponse, String methodName, HashMap requestParameters) {
		AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);
		String requestId = UUID.randomUUID().toString();
		String responseData = "";
		String jsonParams = " ";
		String pathToRequests = getPathToRequestNode(pathToResponse);
		LOG.info("Path To response node: {}", pathToResponse);
		try {
			requestParameters.put("responsePath", pathToResponse);
			jsonParams = new ObjectMapper().writeValueAsString(requestParameters);
		}catch (Exception e) {
			LOG.error("Problem converting parameters map to json");
			e.printStackTrace();
		}
		String requestJson = "{\"jsonrpc\":\"2.0\",\"method\":\"" + methodName + "\",\"params\":\""+ jsonParams + "\",\"id\":\"" + requestId + "\"}";
		try {
			checkIfRequestResponseNodeExist(pathToRequests, pathToResponse);
			SimpleDistributedQueue data = new SimpleDistributedQueue(client, pathToRequests);
			if (data.offer(requestJson.getBytes())) {
				LOG.info("Writing request to node successful {}", requestJson);
			}
			LOG.info("Waiting for {} response", methodName);
			async.with(WatchMode.successOnly).watched().getData().forPath(pathToResponse).event()
					.thenAccept(event -> {
						LOG.info("ET:" + event.getType());
						LOG.info("E:" + event);
					}).toCompletableFuture().get(4, TimeUnit.SECONDS);

		} catch (Exception e) {
			LOG.error("Problem on reading response from {}", methodName);
			e.printStackTrace();
		}
		return responseData;
	}

	private String getPathToRequestNode(String pathToData){
		String requestNodePath = "";
		int count=0;
		String[] nodeParts = pathToData.split("/");
		for(String partOfPath : nodeParts) {
			count++;
			if(count<7) {
				requestNodePath = requestNodePath + partOfPath + "/";
			}
			else{
				break;
			}
		}
		requestNodePath = requestNodePath + "request";
		LOG.info("Path to request node: {}", requestNodePath);
		return requestNodePath;
	}

}
