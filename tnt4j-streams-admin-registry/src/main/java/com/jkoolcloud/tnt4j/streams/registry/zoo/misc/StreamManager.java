package com.jkoolcloud.tnt4j.streams.registry.zoo.misc;

import java.util.Map;
import java.util.Set;

import org.apache.curator.framework.CuratorFramework;

public class StreamManager {

	private Map<String, CuratorFramework> stringCuratorFrameworkMap;

	public StreamManager(Map<String, CuratorFramework> stringCuratorFrameworkMap) {
		this.stringCuratorFrameworkMap = stringCuratorFrameworkMap;
	}

	public CuratorFramework getConnection(String name) {
		return stringCuratorFrameworkMap.get(name);
	}

	public void putStream(String streamName, CuratorFramework curatorFramework) {
		stringCuratorFrameworkMap.put(streamName, curatorFramework);
	}

	public void closeStream(String streamName) {
		stringCuratorFrameworkMap.get(streamName).close();
	}

	public void startStream(String streamName) {
		stringCuratorFrameworkMap.get(streamName).start();
	}

	public Set<String> getStreamNamesSet() {
		Set<String> streamsSet = stringCuratorFrameworkMap.keySet();
		return streamsSet;
	}

}
