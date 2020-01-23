package com.jkoolcloud.tnt4j.streams.registry.zoo.jmx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JmxConnRegistry {

	private Map<String, JmxConn> streamAgentNameToJmxConn = new ConcurrentHashMap<>();

	public void add(String key, JmxConn jmxConn) {
		streamAgentNameToJmxConn.put(key, jmxConn);
	}

	public JmxConn get(String key) {
		return streamAgentNameToJmxConn.get(key);
	}

	public void remove(String name) {
		streamAgentNameToJmxConn.remove(name);
	}

	public String generateJsonSnapshot() {
		return "not implemented";
	}

}
