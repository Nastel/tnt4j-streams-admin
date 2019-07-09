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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * The type Zookeeper registry endpoint.
 */
@ApplicationScoped
@Path("/registry")
public class ZookeeperRegistryEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperRegistryEndpoint.class);

	private static final String ZOOKEEPER_URL = "172.16.6.51:2181";

	// Logger.getLogger(ZookeeperRegistryEndpoint.class.getName()) ;

	@Inject
	private ZookeeperAccessService zookeeperAccessService;

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
	 * Gets streams services.
	 *
	 * @return the streams services
	 */
	@GET
	@Path("/services")
	@Produces("application/json")
	public String getStreamsServices() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			LOG.info("-------->Accessing zookeeper:{}", zookeeperAccessService);
			return mapper.writeValueAsString(zookeeperAccessService.getStreamServices());

		} catch (Exception e) {
			LOG.error("Error on services registry access from zookeeper", e);
			return null;
		}
	}

	/**
	 * Gets stream services node tree from cluster to the latest nodes for front-end navigational view
	 *
	 * @return the latest ZooKeeper node tree view
	 */
	@GET
	@Path("/nodeTree")
	@Produces("application/json")
	public String getStreamServicesNodeTree() {

		ObjectMapper mapper = new ObjectMapper();
		try {
			LOG.info("-->Creating ZooKeeper Node Tree<--");
			String value = mapper.writeValueAsString(zookeeperAccessService.getTreeNodes());
			return value;

		} catch (Exception e) {
			LOG.error("Error on services registry access from zookeeper", e);
			return null;
		}
	}


	/**
	 * Gets stream services node information
	 *
	 * @return the node information
	 */
	@GET
	@Path("{nodePath:.*}/list")
	public Response getWildcardList(@PathParam("nodePath") List<PathSegment> nodePath) {
		LOG.info("Call url parameters{}", nodePath);
		ObjectMapper mapper = new ObjectMapper();
		String pathToNode ="";
		try {
			for (PathSegment node : nodePath) {
				pathToNode = pathToNode + "/" + node;
			}
			LOG.info("Path created from URL: {}", pathToNode);
			String value = mapper.writeValueAsString(zookeeperAccessService.getServiceNodeData(pathToNode));

			return Response.status(200).entity(value).build();
		} catch (Exception e) {
			LOG.error("Error on reading node from ZooKeeper", e);
			return null;
		}
	}


	/* ------------------------------- V1 ------------------------------------------------*/

	/**
	 * Gets stream services static data.
	 *
	 * @return the stream services static data
	 */
	@GET
	@Path("/config")
	@Produces("application/json")
	public String getStreamServicesStaticData() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			LOG.info("-------->Accessing zookeeper:{}", zookeeperAccessService);
			return mapper.writeValueAsString(zookeeperAccessService.getStreamServicesStaticData());

		} catch (Exception e) {
			LOG.error("Error on services registry access from zookeeper", e);
			return null;
		}
	}

}
