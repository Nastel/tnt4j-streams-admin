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
