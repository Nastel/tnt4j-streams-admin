package com.jkoolcloud.tnt4j.streams.registry.zoo.misc;

import java.util.HashMap;

public class StreamManagerSingleton {

	private static StreamManager streamManager;

	private StreamManagerSingleton() {
	}

	public static synchronized StreamManager getInstance() {
		if (streamManager == null) {
			streamManager = new StreamManager(new HashMap<>());
		}
		return streamManager;
	}

}
