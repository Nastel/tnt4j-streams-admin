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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.RuntimeInfo;
import com.jkoolcloud.tnt4j.streams.registry.zoo.runtime.RuntimeInfoWrapper;

public class JmxConnRemote implements StreamControls {

	private JMXServiceURL serviceUrl;
	private JMXConnector jmxConnector;
	private MBeanServerConnection mbeanConn;
	private JmxStatistics jmxStatistics;

	// private static final EventSink JmxConnEventSink = LoggerUtils.getLoggerSink("JmxConn");
	private static final Logger JmxConnEventSink = LoggerFactory.getLogger("JmxConn");

	public JmxConnRemote(String host, int port) {
		String connStr = createConnStr(host, port);
		connect(connStr);

	}

	private String createConnStr(String host, int port) {
		return "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
	}

	private void connect(String connStr) {
		try {
			serviceUrl = new JMXServiceURL(connStr);
		} catch (MalformedURLException e) {
			// JmxConnEventSink.log(OpLevel.ERROR, "Malformed connection string for JMXServiceURL", e);
			JmxConnEventSink.error("Malformed connection string for JMXServiceURL", e);
		}

		try {
			jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
		} catch (IOException e) {
			JmxConnEventSink.error("Could not connect to the machine with given JMXServiceURL", e);
		}

		try {
			mbeanConn = jmxConnector.getMBeanServerConnection();
		} catch (IOException e) {
			JmxConnEventSink.error("Could not connect to MBeanServer with given jmxConnector", e);
		}

		jmxStatistics = new JmxStatistics(mbeanConn);
	}

	public RuntimeInfo getRuntime() {

		Map<String, Object> osMap = jmxStatistics.getOsInfo();
		Map<String, Object> network = jmxStatistics.getNetworkInfo();
		Map<String, Object> cpu = jmxStatistics.getCpuInfo();
		Map<String, Object> memory = jmxStatistics.getServerPhysicalMemory();
		Map<String, Object> streamsAgentCpuLoad = jmxStatistics.getThreadInfo();
		Map<String, Object> streamsAgentMemory = jmxStatistics.getStreamsAgentMemoryProperties();
		Map<String, Object> disk = jmxStatistics.getDisk();
		Map<String, Object> versions = jmxStatistics.getVersionsProperties();
		Map<String, Object> configs = jmxStatistics.getConfigs();
		Map<String, Object> service = RuntimeInfoWrapper.getServiceProperties();

		RuntimeInfo runtimeInfo = new RuntimeInfo();
		runtimeInfo.setOs(osMap);
		runtimeInfo.setNetwork(network);
		runtimeInfo.setCpu(cpu);
		runtimeInfo.setMemory(memory);
		runtimeInfo.setStreamsAgentCpu(streamsAgentCpuLoad);
		runtimeInfo.setStreamsAgentMemory(streamsAgentMemory);
		runtimeInfo.setDisk(disk);
		runtimeInfo.setVersions(versions);
		runtimeInfo.setConfigs(configs);
		runtimeInfo.setService(service);

		return runtimeInfo;
	}

	@Override
	public void pauseAll() {

	}

	@Override
	public void pause(String names) {

	}

	@Override
	public void stopAll() {

	}

	@Override
	public void stop(String names) {

	}

	@Override
	public void restartAll() {

	}

	@Override
	public void restart(String names) {

	}

	@Override
	public String[] getRunningStreamNames() {
		return new String[0];
	}

	@Override
	public boolean isRunning() {
		return false;
	}
}
