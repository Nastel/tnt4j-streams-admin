package com.jkoolcloud.tnt4j.streams.admin.backend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.ZookeeperAccessService;

public class RequestPath {

	public RequestPath() {
	}

	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperAccessService.class);

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
		responseNodePath = responseNodePath + "response";
		LOG.info("Path to response node: {}", responseNodePath);
		return responseNodePath;
	}

	public String getClusterNameFromPath(String pathToData) {
		String agentName = "";
		int count = 0;
		String[] nodeParts = pathToData.split("/");
		for (String partOfPath : nodeParts) {
			count++;
			if (count == 5) {
				agentName = partOfPath;
			}
		}
		LOG.info("Path to request node: {}", agentName);
		return agentName;
	}

	public String getAgentNameFromPath(String pathToData) {
		String agentName = "";
		int count = 0;
		String[] nodeParts = pathToData.split("/");
		for (String partOfPath : nodeParts) {
			count++;
			if (count == 6) {
				agentName = partOfPath;
			}
		}
		LOG.info("Path to request node: {}", agentName);
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
		LOG.info("Path to request node: {}", agentName);
		return agentName;
	}

}
