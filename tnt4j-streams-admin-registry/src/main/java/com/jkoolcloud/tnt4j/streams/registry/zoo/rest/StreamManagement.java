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

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.jkoolcloud.tnt4j.streams.registry.zoo.watcher.StreamMonitoringService;
import com.jkoolcloud.tnt4j.streams.registry.zoo.watcher.WatchDogPolling;

@Path("/")
public class StreamManagement {

	private WatchDogPolling watchDogPolling = StreamMonitoringService.getInstance();

	// TODO logging and handling
	@PostConstruct
	public void init() {
		try {
			watchDogPolling.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@POST
	@Path("/streamsAgent/streams/{stream}/start")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startStream(@PathParam("stream") String stream) {
		watchDogPolling.startStream(stream);
		return Response.status(Response.Status.OK).build();
	}

	@POST
	@Path("/streamsAgent/streams/{stream}/stop")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopStream(@PathParam("stream") String stream) {
		watchDogPolling.closeStream(stream);
		return Response.status(Response.Status.OK).build();
	}

	@GET
	@Path("/streamsAgent/streams")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRunnableStreams() {
		return Response.status(Response.Status.OK).entity(watchDogPolling.getStreamRegistry()).build();
	}
}
