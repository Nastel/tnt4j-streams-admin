/*
 * Copyright 2014-2020 JKOOL, LLC.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPath {
	private static final Logger LOG = LoggerFactory.getLogger(RequestPath.class);

	public RequestPath() {
	}

	/**
	 * Returns request node path from full path
	 * 
	 * @param pathToData
	 * @return
	 */
	public static String getPathToRequestNode(String pathToData) {
		String requestNodePath = "";
		int count = 0;
		try {
			int AGENT_DEPTH = Integer.parseInt(PropertyData.getProperty("depthToAgentNode"));
			String[] nodeParts = pathToData.split("/");
			for (String partOfPath : nodeParts) {
				count++;
				if (count < AGENT_DEPTH + 1) {
					requestNodePath = requestNodePath + partOfPath + "/";
				} else {
					break;
				}
			}
		} catch (Exception e) {
			LOG.error("Problem on getting cluster name");
		}
		requestNodePath = requestNodePath + "requests";
		LOG.info("Path to request node: {}", requestNodePath);
		return requestNodePath;
	}

	/**
	 * Returns response node path from full path
	 * 
	 * @param pathToData
	 * @return
	 */
	public static String getPathToResponseNode(String pathToData) {
		String responseNodePath = "";
		int count = 0;
		try {
			int AGENT_DEPTH = Integer.parseInt(PropertyData.getProperty("depthToAgentNode"));
			String[] nodeParts = pathToData.split("/");
			for (String partOfPath : nodeParts) {
				count++;
				if (count < AGENT_DEPTH + 1) {
					responseNodePath = responseNodePath + partOfPath + "/";
				} else {
					break;
				}
			}
		} catch (Exception e) {
			LOG.error("Problem on getting cluster name");
		}
		responseNodePath = responseNodePath + "responses";
		LOG.info("Path to response node: {}", responseNodePath);
		return responseNodePath;
	}

	/**
	 * Returns cluster node from full path
	 * 
	 * @param pathToData
	 * @return
	 */
	public static String getClusterNameFromPath(String pathToData) {
		String agentName = "";
		try {
			int AGENT_DEPTH = Integer.parseInt(PropertyData.getProperty("depthToAgentNode"));
			int count = 0;
			String[] nodeParts = pathToData.split("/");
			for (String partOfPath : nodeParts) {
				count++;
				if (count == AGENT_DEPTH - 1) {
					agentName = partOfPath;
				}
			}
			LOG.info("Stream name: {}", agentName);
		} catch (Exception e) {
			LOG.error("Problem on getting cluster name");
		}
		return agentName;
	}

	/**
	 * Returns agent node from full path
	 * 
	 * @param pathToData
	 * @return
	 */
	public static String getAgentNameFromPath(String pathToData) {
		String agentName = "";
		try {
			int AGENT_DEPTH = Integer.parseInt(PropertyData.getProperty("depthToAgentNode"));
			int count = 0;
			String[] nodeParts = pathToData.split("/");
			for (String partOfPath : nodeParts) {
				count++;
				if (count == AGENT_DEPTH) {
					agentName = partOfPath;
				}
			}
			LOG.info("Stream name: {}", agentName);
		} catch (Exception e) {
			LOG.error("Problem on getting agent name");
		}
		return agentName;
	}

	/**
	 * Returns stream node from full path
	 * 
	 * @param pathToData
	 * @return
	 */
	public static String getStreamNameFromPath(String pathToData) {
		String streamName = "";
		try {
			int AGENT_DEPTH = Integer.parseInt(PropertyData.getProperty("depthToAgentNode"));
			int count = 0;
			String[] nodeParts = pathToData.split("/");
			for (String partOfPath : nodeParts) {
				count++;
				if (count == AGENT_DEPTH + 1) {
					streamName = partOfPath;
				}
			}
			LOG.info("Stream name: {}", streamName);
		} catch (Exception e) {
			LOG.error("Problem on getting stream name");
		}
		return streamName;
	}

	/**
	 * Returns specified part of path by number node from full path
	 * 
	 * @param pathToData
	 * @return
	 */
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

	/**
	 * Returns path of specified length
	 * 
	 * @param pathToData
	 * @return
	 */
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
