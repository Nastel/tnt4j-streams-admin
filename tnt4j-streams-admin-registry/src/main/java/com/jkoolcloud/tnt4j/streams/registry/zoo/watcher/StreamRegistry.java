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

package com.jkoolcloud.tnt4j.streams.registry.zoo.watcher;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JsonUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.watcher.response.Configuration;
import com.jkoolcloud.tnt4j.streams.registry.zoo.watcher.response.Root;

public class StreamRegistry {

	private Map<String, Stream> directoryToStreamList = new ConcurrentHashMap<>();

	public void add(Stream stream) {
		directoryToStreamList.put(UUID.randomUUID().toString(), stream);
	}

	public void addRunScript(String name, Path runScript) {
		Stream stream = get(name);
		stream.setRunScript(runScript);
		directoryToStreamList.put(name, stream);
	}

	public Stream get(String name) {
		return directoryToStreamList.get(name);
	}

	public void remove(String name) {
		directoryToStreamList.remove(name);
	}

	public String generateJsonSnapshot() {
		Root root = new Root();
		for (Map.Entry<String, Stream> entry : directoryToStreamList.entrySet()) {
			String location = entry.getKey();
			List<String> streams = entry.getValue().getStreams();
			boolean running = entry.getValue().isRunning();
			root.addConfiguration(new Configuration(location, streams, running));

		}

		return JsonUtils.objectToString(root);
	}

	public void startStream(String streamName) {
		Stream stream = directoryToStreamList.get(streamName);

		if (stream == null) {
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
		}

		stream.start();
	}

	public void stopStream(String streamName) {
		Stream stream = directoryToStreamList.get(streamName);

		if (stream == null) {
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
		}

		stream.close();
	}

}
