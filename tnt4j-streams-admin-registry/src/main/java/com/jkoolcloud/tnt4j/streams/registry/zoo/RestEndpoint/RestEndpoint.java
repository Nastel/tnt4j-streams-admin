package com.jkoolcloud.tnt4j.streams.registry.zoo.RestEndpoint;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.StreamsAgent;
import com.jkoolcloud.tnt4j.streams.registry.zoo.stats.AgentStats;
import com.jkoolcloud.tnt4j.streams.registry.zoo.stats.ClusterStats;
import com.jkoolcloud.tnt4j.streams.registry.zoo.stats.StreamControls;
import com.jkoolcloud.tnt4j.streams.registry.zoo.stats.StreamStats;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;

@Path("/")
public class RestEndpoint {

	@GET
	@Path("/clusters")
	@Produces(MediaType.APPLICATION_JSON)
	public String clusters() {
		return ClusterStats.getClusters();
	}

	@GET
	@Path("/cluster")
	@Produces(MediaType.APPLICATION_JSON)
	public String cluster() {
		return ClusterStats.getCluster();
	}

	@GET
	@Path("/streamsAgent")
	@Produces(MediaType.APPLICATION_JSON)
	public String streamAgent() {
		return AgentStats.agentRuntime();
	}

	@GET
	@Path("/streamsAgent/configs")
	@Produces(MediaType.APPLICATION_JSON)
	public String configs() {
		return AgentStats.getConfigs();
	}

	@GET
	@Path("/streamsAgent/logs")
	@Produces(MediaType.APPLICATION_JSON)
	public String logs() {
		return AgentStats.getLogs();
	}

	@GET
	@Path("/streamsAgent/runtimeInformation")
	@Produces(MediaType.APPLICATION_JSON)
	public String runtime() {
		return AgentStats.runtimeInformation();
	}

	@GET
	@Path("/streamsAgent/samples")
	@Produces(MediaType.APPLICATION_JSON)
	public String samples() {
		return AgentStats.getSamples();
	}

	@GET
	@Path("/streamsAgent/threadDump")
	@Produces(MediaType.APPLICATION_JSON)
	public String threadDump() {
		return AgentStats.getThreadDump();
	}

	@GET
	@Path("/streamsAgent/downloadables")
	@Produces(MediaType.APPLICATION_JSON)
	public String downloadables() {
		return AgentStats.getDownloadables();
	}

	@GET
	@Path("/streamsAgent/streamsAndMetrics")
	@Produces(MediaType.APPLICATION_JSON)
	public String allStreamsAndMetrics() {
		return AgentStats.getAllStreamsAndMetricsJson();
	}

	@GET
	@Path("/streamsAgent/{streamName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String stream(@PathParam("streamName") String streamName) {
		return StreamStats.getMetricsForStreamNode(streamName);
	}

	@GET
	@Path("/streamsAgent/downloadables/{file}")
	@Produces(MediaType.APPLICATION_JSON)
	public String download(@PathParam("file") String fileName) {
		return AgentStats.getFile(fileName);
	}

	@GET
	@Path("/streamsAgent/{streamName}/incomplete")
	@Produces(MediaType.APPLICATION_JSON)
	public String incomplete(@PathParam("streamName") String streamName) {
		return StreamStats.getIncomplete(streamName);
	}

	@GET
	@Path("/streamsAgent/{streamName}/metrics")
	@Produces(MediaType.APPLICATION_JSON)
	public String metrics(@PathParam("streamName") String streamName) {
		return StreamStats.getMetricsForNode(streamName);
	}

	@GET
	@Path("/streamsAgent/{streamName}/repository")
	@Produces(MediaType.APPLICATION_JSON)
	public String repository(@PathParam("streamName") String streamName) {
		return StreamStats.getRepositoryStatus(streamName);
	}

	@GET
	@Path("streamsAgent/{streamName}/start")
	@Produces(MediaType.APPLICATION_JSON)
	public String start(@PathParam("streamName") String streamName) {
		StreamControls.restartStreams(streamName);
		return "started";
	}

	@GET
	@Path("streamsAgent/{streamName}/stop")
	@Produces(MediaType.APPLICATION_JSON)
	public String stop(@PathParam("streamName") String streamName) {
		StreamControls.stopStream(streamName);
		return "stopped";
	}

	@GET
	@Path("streamsAgent/{streamName}/replay")
	@Produces(MediaType.APPLICATION_JSON)
	public String replay(@QueryParam("b") String blocks) {

		String[] blocksArr = blocks.split(",");

		for (String block : blocksArr) {
			if (!block.matches("[0-9]+")) {
				LoggerWrapper.addMessage(OpLevel.ERROR, String.format("Bad format: %s", blocks));
				return "{ \"error\" : \" Wrong format\" }";
			}
		}

		// StreamControls.processRequest(blocksArr);
		return blocks;
	}

	@GET
	@Path("/ping")
	@Produces(MediaType.APPLICATION_JSON)
	public String ping() {
		System.out.println("Pong");
		StreamsAgent.getRunningStreamNames();
		return "pong";
	}

}
