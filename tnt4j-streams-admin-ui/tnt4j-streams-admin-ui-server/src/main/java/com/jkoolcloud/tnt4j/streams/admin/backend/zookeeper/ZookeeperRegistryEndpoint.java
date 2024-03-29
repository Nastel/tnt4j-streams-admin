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

package com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper;

import java.util.HashMap;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth.LoginCache;
import com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth.UsersUtils;

/**
 * The type Zookeeper registry endpoint.
 */
@ApplicationScoped
@Path("/registry")
public class ZookeeperRegistryEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperRegistryEndpoint.class);

	LoginCache loginCache = new LoginCache();
	@Inject
	private ZookeeperAccessService zookeeperAccessService;

	@Inject
	private UsersUtils usersUtils;

	/**
	 * Gets info.
	 *
	 * @return the info
	 */
	@GET
	public String getInfo() {
		return "Streams Services Registry endpoint";
	}

	/**
	 * Gets stream services node tree from cluster to the latest nodes for front-end navigational view
	 *
	 * @return the latest ZooKeeper node tree view
	 */
	@GET
	@Path("/nodeTree")
	@Produces("application/json")
	public Response getStreamServicesNodeTree(@HeaderParam("Authorization") String authHeader) {
		// LOG.info("Header auth token for tree data {}", authHeader);
		if (checkIfUserExistAndBypassLogin(authHeader)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				String value = mapper.writeValueAsString(zookeeperAccessService.getTreeNodes(authHeader));
				return Response.status(200).entity(value).build();

			} catch (Exception e) {
				LOG.error("Error on services registry access from zookeeper", e);
				return Response.status(401).entity("Error on services registry access from zookeeper").build();
			}
		} else {
			LOG.info("Return a 401 inside tree data call");
			return Response.status(401).entity("Tried to access protected resources").build();
		}
	}

	/**
	 * Create request to replay item ( block) dataReading
	 *
	 * @return the node information
	 */
	@GET
	@Path("{nodePath:.*}/blockReplay")

	public Response catchRequestWildcardReplay(@PathParam("nodePath") List<PathSegment> nodePath,
			@HeaderParam("Authorization") String authHeader) {
		ObjectMapper mapper = new ObjectMapper();
		StringBuilder pathToNode = new StringBuilder();
		if (checkIfUserExistAndBypassLogin(authHeader)) {
			try {
				LOG.info("-->Calling replay block request<--");
				for (PathSegment node : nodePath) {
					pathToNode.append("/").append(node);
				}
				HashMap tempMap = zookeeperAccessService.getServiceNodeInfoFromLinkForReplay(pathToNode.toString(),
						authHeader);
				String value = mapper.writeValueAsString(tempMap.get("data"));
				if (value.equals("") || value.isEmpty()) {
					return Response.status(200)
							.entity("{ \"action\" : \"No response received ( action token missing )\"}").build();
				} else {
					LOG.info("Response to block replay request: " + value);
					return Response.status(200).entity("{ \"action\" : " + value + "}").build();
				}
			} catch (Exception e) {
				LOG.error("Error on trying to replay item : ", e);
				return Response.status(200).entity("{ \"action\" : \"Error on trying to replay item\"}").build();
			}
		} else {
			LOG.info("Return a 401 inside node call");
			return Response.status(401).entity("{ \"action\" : \"Tried to access protected resources\"").build();
		}
	}

	/**
	 * Create request to start stream
	 *
	 * @return the node information
	 */
	@GET
	@Path("{nodePath:.*}/start")
	public Response catchRequestWildcardStart(@PathParam("nodePath") List<PathSegment> nodePath,
			@HeaderParam("Authorization") String authHeader) {
		ObjectMapper mapper = new ObjectMapper();
		StringBuilder pathToNode = new StringBuilder();
		if (checkIfUserExistAndBypassLogin(authHeader)) {
			try {
				LOG.info("-->Calling start stream request<--");
				for (PathSegment node : nodePath) {
					pathToNode.append("/").append(node);
				}
				LOG.info("Path created from URL: " + pathToNode.toString());
				HashMap tempMap = zookeeperAccessService.getServiceNodeInfoFromLink(pathToNode.toString(), 0,
						authHeader);
				String value = mapper.writeValueAsString(tempMap.get("data"));
				LOG.info(value);
				if (value.equals("") || value.isEmpty()) {
					return Response.status(200)
							.entity("{ \"action\" : \"No response received ( action token missing )\"}").build();
				} else {
					LOG.info("Response to stream start: " + value);
					return Response.status(200).entity("{ \"action\" : " + value + "}").build();
				}
			} catch (Exception e) {
				LOG.error("Error on reading starting stream", e);
				return Response.status(200).entity("{ \"action\" : \"Error on reading starting stream\"}").build();
			}
		} else {
			LOG.info("Return a 401 inside node call");
			return Response.status(401).entity("{ \"action\" : \"Tried to access protected resources\"").build();
		}
	}

	/**
	 * Create request to stop stream
	 *
	 * @return the node information
	 */
	@GET
	@Path("{nodePath:.*}/stop")
	public Response catchRequestWildcardStop(@PathParam("nodePath") List<PathSegment> nodePath,
			@HeaderParam("Authorization") String authHeader) {
		ObjectMapper mapper = new ObjectMapper();
		StringBuilder pathToNode = new StringBuilder();
		if (checkIfUserExistAndBypassLogin(authHeader)) {
			try {
				LOG.info("-->Calling stop stream request<--");
				for (PathSegment node : nodePath) {
					pathToNode.append("/").append(node);
				}
				HashMap tempMap = zookeeperAccessService.getServiceNodeInfoFromLink(pathToNode.toString(), 0,
						authHeader);
				String value = mapper.writeValueAsString(tempMap.get("data"));
				if (value.equals("") || value.isEmpty()) {
					return Response.status(200)
							.entity("{ \"action\" : \"No response received ( action token missing )\"}").build();
				} else {
					LOG.info("Response to stream stop: " + value);
					return Response.status(200).entity("{ \"action\" : " + value + "}").build();
				}
			} catch (Exception e) {
				LOG.error("Error on reading stopping stream", e);
				return Response.status(200).entity("{ \"action\" : \"Error on reading stopping stream\"}").build();
			}
		} else {
			LOG.info("Return a 401 inside node call");
			return Response.status(401).entity("{ \"action\" : \"Tried to access protected resources\"").build();
		}
	}

	/**
	 * Gets stream services node information
	 *
	 * @return the node information
	 */
	@GET
	@Path("{nodePath:.*}/list")
	public Response getWildcardList(@PathParam("nodePath") List<PathSegment> nodePath,
			@HeaderParam("Authorization") String authHeader) {
		if (checkIfUserExistAndBypassLogin(authHeader)) {
			ObjectMapper mapper = new ObjectMapper();
			StringBuilder pathToNode = new StringBuilder();
			try {
				// LOG.info("-->Calling get node data request for node <-- "+ nodePath);
				for (PathSegment node : nodePath) {
					pathToNode.append("/").append(node);
				}
				String value = mapper.writeValueAsString(
						zookeeperAccessService.getServiceNodeInfoFromLink(pathToNode.toString(), 0, authHeader));
				return Response.status(200).entity(value).build();
			} catch (Exception e) {
				LOG.error("Error on reading node from ZooKeeper", e);
				return Response.status(401).entity("Error on reading node: " + nodePath + " from ZooKeeper").build();
			}
		} else {
			LOG.info("Return a 401 inside node call");
			return Response.status(401).entity("Tried to access protected resources").build();
		}
	}

	/**
	 * Gets stream services node information
	 *
	 * @return the node information
	 */
	@GET
	@Path("{nodePath:.*}/logs/list")
	public Response getWildcardLogData(@PathParam("nodePath") List<PathSegment> nodePath,
			@QueryParam("logCount") int logLineCount, @HeaderParam("Authorization") String authHeader) {
		if (checkIfUserExistAndBypassLogin(authHeader)) {
			ObjectMapper mapper = new ObjectMapper();
			StringBuilder pathToNode = new StringBuilder();
			try {
				for (PathSegment node : nodePath) {
					pathToNode.append("/").append(node);
				}
				pathToNode.append("/logs");
				String value = mapper.writeValueAsString(zookeeperAccessService
						.getServiceNodeInfoFromLink(pathToNode.toString(), logLineCount, authHeader));
				return Response.status(200).entity(value).build();
			} catch (Exception e) {
				LOG.error("Error on reading node from ZooKeeper", e);
				return Response.status(401).entity("Error on reading node: " + nodePath + " from ZooKeeper").build();
			}
		} else {
			LOG.info("Return a 401 inside node call");
			return Response.status(401).entity("Tried to access protected resources").build();
		}
	}

	/**
	 * A method used to check if user exists and is already logged in and if true to set so that user would not be
	 * checked for credentials.
	 * 
	 * @param authHeader
	 *            auth token from authHeader for user authentication
	 * @return
	 */
	private boolean checkIfUserExistAndBypassLogin(String authHeader) {
		if (authHeader != null && !authHeader.isEmpty()) {
			if (loginCache.checkIfUserExistInCache(authHeader)) {
				loginCache.setBypassSecurity(true);
				usersUtils.loginTheUserByCredentials("", "");
				return true;
			} else {
				LOG.info("No user with the token provided was found");
				ZooKeeperConnectionManager zooManager = new ZooKeeperConnectionManager();
				zooManager.setConnectionToken(authHeader);
				zooManager.removeClientConnection();
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Rundeck job call for update of stream agent version
	 * 
	 * @param nodePath
	 * @param versionNum
	 * @param authHeader
	 * @return
	 */
	@GET
	@Path("{nodePath:.*}/update/task")
	public Response updateStream(@PathParam("nodePath") List<PathSegment> nodePath,
			@QueryParam("version") String versionNum, @HeaderParam("Authorization") String authHeader) {
		if (checkIfUserExistAndBypassLogin(authHeader)) {
			ObjectMapper mapper = new ObjectMapper();
			StringBuilder pathToNode = new StringBuilder();
			try {
				for (PathSegment node : nodePath) {
					pathToNode.append("/").append(node);
				}
				// String value = mapper.writeValueAsString(
				// String value = mapper.writeValueAsString(
				// zookeeperAccessService.getServiceNodeInfoFromLink(pathToNode.toString(), authHeader));
			} catch (Exception e) {
				LOG.error("Error on reading node from ZooKeeper", e);
				return Response.status(401).entity("Error on reading node: " + nodePath + " from ZooKeeper").build();
			}
		} else {
			LOG.info("Return a 401 inside node call");
			return Response.status(401).entity("Tried to access protected resources").build();
		}
		return Response.status(200).entity("temp").build();
	}
}
