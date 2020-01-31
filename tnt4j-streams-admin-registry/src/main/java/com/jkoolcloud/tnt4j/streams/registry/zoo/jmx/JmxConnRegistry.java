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

package com.jkoolcloud.tnt4j.streams.registry.zoo.jmx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

@Singleton
public class JmxConnRegistry {

	private Map<String, JmxConnRemote> streamAgentNameToJmxConn = new ConcurrentHashMap<>();

	private JmxStreamAgentsDiscovery jmxStreamAgentsDiscovery = new JmxStreamAgentsDiscovery();

	public void add(String key, JmxConnRemote jmxConnRemote) {
		streamAgentNameToJmxConn.put(key, jmxConnRemote);
	}

	public JmxConnRemote get(String key) {
		return streamAgentNameToJmxConn.get(key);
	}

	public JmxConnRegistry() {
	}

	public void remove(String name) {
		streamAgentNameToJmxConn.remove(name);
	}

	public void searchForOfflineAgents() {

	}

	public void searchForLiveAgents() {

	}

}
