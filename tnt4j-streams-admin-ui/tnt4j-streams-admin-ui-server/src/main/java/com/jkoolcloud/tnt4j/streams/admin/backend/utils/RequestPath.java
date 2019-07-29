package com.jkoolcloud.tnt4j.streams.admin.backend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.ZookeeperAccessService;

public class RequestPath {

	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperAccessService.class);

	public RequestPath() {
	}

	public static String getPathToRequestNode(String pathToData) {
		String requestNodePath = "";
		int count = 0;
		String[] nodeParts = pathToData.split("/");
		for (String partOfPath : nodeParts) {
			count++;
			if (count < 7) {
				requestNodePath = requestNodePath + partOfPath + "/";
			} else {
				break;
			}
		}
		requestNodePath = requestNodePath + "requests";
		LOG.info("Path to request node: {}", requestNodePath);
		return requestNodePath;
	}

	public static String getPathToResponseNode(String pathToData) {
		String responseNodePath = "";
		int count = 0;
		String[] nodeParts = pathToData.split("/");
		for (String partOfPath : nodeParts) {
			count++;
			if (count < 7) {
				responseNodePath = responseNodePath + partOfPath + "/";
			} else {
				break;
			}
		}
		responseNodePath = responseNodePath + "responses";
		LOG.info("Path to response node: {}", responseNodePath);
		return responseNodePath;
	}

	public static String getClusterNameFromPath(String pathToData) {
		String agentName = "";
		int count = 0;
		String[] nodeParts = pathToData.split("/");
		for (String partOfPath : nodeParts) {
			count++;
			if (count == 5) {
				agentName = partOfPath;
			}
		}
		LOG.info("Cluster name: {}", agentName);
		return agentName;
	}

	public static String getAgentNameFromPath(String pathToData) {
		String agentName = "";
		int count = 0;
		String[] nodeParts = pathToData.split("/");
		for (String partOfPath : nodeParts) {
			count++;
			if (count == 6) {
				agentName = partOfPath;
			}
		}
		LOG.info("Agent name: {}", agentName);
		return agentName;
	}

	public static String getStreamNameFromPath(String pathToData) {
		String agentName = "";
		int count = 0;
		String[] nodeParts = pathToData.split("/");
		for (String partOfPath : nodeParts) {
			count++;
			if (count == 7) {
				agentName = partOfPath;
			}
		}
		LOG.info("Stream name: {}", agentName);
		return agentName;
	}

	public static String getTheSpecifiedEndpointPart(String pathToData, int pathPartNumber) {
		String agentName = "";
		int count = 0;
		String[] nodeParts = pathToData.split("/");
		for (String partOfPath : nodeParts) {
			count++;
			if (count == pathPartNumber) {
				agentName = partOfPath;
			}
		}
		LOG.info("Path to request node: {}", agentName);
		return agentName;
	}

	public static String getPathOfSpecifiedLength(String pathToData, int pathPartNumber) {
		String nodePath = "";
		int count = 0;
		String[] nodeParts = pathToData.split("/");
		for (String partOfPath : nodeParts) {
			count++;
			if (count < pathPartNumber) {
				nodePath = nodePath + partOfPath + "/";
			} else {
				break;
			}
		}
		LOG.info("Path to response node: {}", nodePath);
		return nodePath;
	}
}
