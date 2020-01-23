package com.jkoolcloud.tnt4j.streams.registry.zoo.watcher;

import com.jkoolcloud.tnt4j.streams.registry.zoo.Init;

public class StreamMonitoringService {

	// TODO find better way to pass path argument
	private static WatchDogPolling watchDogPolling = new WatchDogPolling(Init.getPaths().getSampleDirectoryPath());

	public static WatchDogPolling getInstance() {
		return watchDogPolling;
	}

	private StreamMonitoringService() {
	}
}
