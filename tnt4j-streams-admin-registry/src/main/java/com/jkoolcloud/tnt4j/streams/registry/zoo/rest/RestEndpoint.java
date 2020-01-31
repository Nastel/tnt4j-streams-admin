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

package com.jkoolcloud.tnt4j.streams.registry.zoo.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.jkoolcloud.tnt4j.streams.registry.zoo.authentication.SecureAction;
import com.jkoolcloud.tnt4j.streams.registry.zoo.authentication.SecureRead;

@Path("/")
public class RestEndpoint {

	// @Inject
	// JmxConnRegistry jmxConnRegistry;

	private Integer integer = new Integer(0);

	@GET
	@Path("/ping")
	@Produces(MediaType.TEXT_PLAIN)
	// @SecureRead
	public Response ping() {

		System.out.println();

		System.out.println(integer);

		return Response.status(Response.Status.OK).entity(" ").build();
	}

	@GET
	@Path("/streamsAgent")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureRead
	public String streamAgent() {
		return null;// AgentStats.agentRuntime();
	}

	@GET
	@Path("/streamsAgent/configs")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureRead
	public String configs() {
		return null;// AgentStats.getConfigs();
	}

	@GET
	@Path("/streamsAgent/logs")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureRead
	public String logs() {
		return null;// AgentStats.getLogs();
	}

	@GET
	@Path("/streamsAgent/runtimeInformation")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureRead
	public String runtime() {
		return null;// AgentStats.runtimeInformation();
	}

	@GET
	@Path("/streamsAgent/samples")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureRead
	public String samples() {
		return null;// AgentStats.getSamples();
	}

	@GET
	@Path("/streamsAgent/threadDump")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureRead
	public String threadDump() {
		return null;// AgentStats.getThreadDump();
	}

	@GET
	@Path("/streamsAgent/downloadables")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureRead
	public String downloadables() {
		return null;// AgentStats.getDownloadables();
	}

	@GET
	@Path("/streamsAgent/streamsAndMetrics")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureRead
	public String allStreamsAndMetrics() {
		return null;// AgentStats.getAllStreamsAndMetricsJson();
	}

	@GET
	@Path("/streamsAgent/{streamName}")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureRead
	public String stream(@PathParam("streamName") String streamName) {
		return null;// StreamStats.getMetricsForStreamNode(streamName);
	}

	@GET
	@Path("/streamsAgent/downloadables/{file}")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureRead
	public String download(@PathParam("file") String fileName) {
		return null;// AgentStats.getFile(fileName);
	}

	/*
	 * +
	 * 
	 * @GET
	 * 
	 * @Path("/streamsAgent/{streamName}/incomplete")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON)
	 * 
	 * @SecureRead public String incomplete(@PathParam("streamName") String streamName) { // return
	 * StreamStats.getIncomplete(streamName); return "k"; }
	 * 
	 * @GET
	 * 
	 * @Path("/streamsAgent/{streamName}/repository")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON)
	 * 
	 * @SecureRead public String repository(@PathParam("streamName") String streamName) { // return
	 * StreamStats.getRepositoryStatus(streamName); return "k"; }
	 */

	@GET
	@Path("/streamsAgent/{streamName}/metrics")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureRead
	public String metrics(@PathParam("streamName") String streamName) {
		return null;// StreamStats.getMetricsForNode(streamName);
	}

	@GET
	@Path("streamsAgent/{streamName}/start")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureAction
	public String start(@PathParam("streamName") String streamName) {
		// StreamControls.restartStreams(streamName);
		return "started";
	}

	@GET
	@Path("streamsAgent/{streamName}/stop")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureAction
	public String stop(@PathParam("streamName") String streamName) {
		// StreamControls.stopStream(streamName);
		return "stopped";
	}

	@GET
	@Path("streamsAgent/{streamName}/replay")
	@Produces(MediaType.APPLICATION_JSON)
	@SecureAction
	public String replay(@QueryParam("b") String blocks) {

		String[] blocksArr = blocks.split(",");

		for (String block : blocksArr) {
			if (!block.matches("[0-9]+")) {
				// LoggerWrapper.addMessage(OpLevel.ERROR, String.format("Bad format: %s", blocks));
				return "{ \"error\" : \" Wrong format\" }";
			}
		}
		// StreamControls.processRequest(blocksArr);

		return blocks;
	}

}
