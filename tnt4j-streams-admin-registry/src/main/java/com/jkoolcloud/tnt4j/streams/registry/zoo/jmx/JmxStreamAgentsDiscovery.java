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

import java.io.File;
import java.io.IOException;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class JmxStreamAgentsDiscovery {

	private static final String CONNECTOR_ADDRESS_PROPERTY = "com.sun.management.jmxremote.localConnectorAddress";

	private static final String STREAMS_AGENT_NAME = "com.jkoolcloud.tnt4j.streams.StreamsAgent";

	public List<VirtualMachineDescriptor> searchStreamAgents() {
		List<VirtualMachineDescriptor> list = VirtualMachine.list();
		List<VirtualMachineDescriptor> streamsAgents = new ArrayList<>();
		for (VirtualMachineDescriptor vmd : list) {
			if (vmd.displayName().contains(STREAMS_AGENT_NAME)) {
				streamsAgents.add(vmd);
			}
		}
		return streamsAgents;
	}

	public JMXConnector getLocalConnection(VirtualMachine vm) throws Exception {
		Properties props = vm.getAgentProperties();
		String connectorAddress = props.getProperty(CONNECTOR_ADDRESS_PROPERTY);
		if (connectorAddress == null) {
			props = vm.getSystemProperties();
			String home = props.getProperty("java.home");
			String agent = home + File.separator + "lib" + File.separator + "management-agent.jar";
			vm.loadAgent(agent);
			props = vm.getAgentProperties();
			connectorAddress = props.getProperty(CONNECTOR_ADDRESS_PROPERTY);
		}
		JMXServiceURL url = new JMXServiceURL(connectorAddress);
		return JMXConnectorFactory.connect(url);
	}

	public List<JMXConnector> produce() {

		List<VirtualMachineDescriptor> vmds = searchStreamAgents();

		List<JMXConnector> jmxConnectors = new ArrayList<>();
		for (VirtualMachineDescriptor vmd : vmds) {

			VirtualMachine vm = null;
			try {
				vm = VirtualMachine.attach(vmd);
			} catch (AttachNotSupportedException | IOException e) {
				e.printStackTrace();
				continue;
			}

			JMXConnector jmxConnector = null;
			try {
				jmxConnector = getLocalConnection(vm);
			} catch (Exception e) {
				e.printStackTrace();
			}

			jmxConnectors.add(jmxConnector);
		}

		return jmxConnectors;
	}

	private String generateKeyFromPath(JMXConnector jmxConnector) {
		MBeanServerConnection mbeanConn = null;
		try {
			mbeanConn = jmxConnector.getMBeanServerConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ObjectName objectName = null;
		try {
			objectName = new ObjectName("java.lang:type=Runtime");
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		}

		RuntimeMXBean runtimeMXBean = JMX.newMXBeanProxy(mbeanConn, objectName, RuntimeMXBean.class, true);

		Map<String, String> systemProperties = runtimeMXBean.getSystemProperties();

		Path path = Paths.get(systemProperties.get("sun.java.command"));

		systemProperties.get("tnt4j.config");
		systemProperties.get("log4j.configuration");
		systemProperties.get("user.dir");

		int directoriesBack = 2;
		int subpathStart = path.getNameCount() - directoriesBack;
		return path.subpath(subpathStart, path.getNameCount()).toString();
	}

	public static void main(String[] args) throws Exception {
		JmxStreamAgentsDiscovery jmxStreamAgentsDiscovery = new JmxStreamAgentsDiscovery();
		List<VirtualMachineDescriptor> virtualMachineDescriptors = jmxStreamAgentsDiscovery.searchStreamAgents();

		for (VirtualMachineDescriptor vmd : virtualMachineDescriptors) {
			VirtualMachine virtualMachine = VirtualMachine.attach(vmd);

			JMXConnector jmxConnector = jmxStreamAgentsDiscovery.getLocalConnection(virtualMachine);

			MBeanServerConnection mbeanConn = jmxConnector.getMBeanServerConnection();

			ObjectName objectName = new ObjectName("java.lang:type=Runtime");

			RuntimeMXBean runtimeMXBean = JMX.newMXBeanProxy(mbeanConn, objectName, RuntimeMXBean.class, true);

			Map<String, String> systemProperties = runtimeMXBean.getSystemProperties();

			System.out.println();
			System.out.println(systemProperties.get("sun.java.command"));
			System.out.println(systemProperties.get("tnt4j.config"));
			System.out.println(systemProperties.get("log4j.configuration"));
			System.out.println(systemProperties.get("user.dir"));

		}
	}

}

/*
 * System.out.println(); System.out.println(systemProperties.get("sun.java.command"));
 * System.out.println(systemProperties.get("tnt4j.config"));
 * System.out.println(systemProperties.get("log4j.configuration"));
 * System.out.println(systemProperties.get("user.dir"));
 * 
 * 
 * com.jkoolcloud.tnt4j.streams.StreamsAgent
 * -f:C:\development\tnt4j\k2\development\k2-index-search\tnt4j-streams-k2\samples\ethereum\ethereumInfura.xml
 * C:\development\tnt4j\k2\development\k2-index-search\tnt4j-streams-k2\config\tnt4j.properties
 * file:C:\development\tnt4j\k2\development\k2-index-search\tnt4j-streams-k2\config\log4jReplay.properties
 * C:\development\tnt4j\k2\development\k2-index-search\tnt4j-streams-k2
 * 
 * com.jkoolcloud.tnt4j.streams.StreamsAgent
 * -f:C:\development\tnt4j\k2\development\k2-index-search\tnt4j-streams-k2\samples\ethereum\ethereumInfura.xml
 * C:\development\tnt4j\k2\development\k2-index-search\tnt4j-streams-k2\config\tnt4j.properties
 * file:C:\development\tnt4j\k2\development\k2-index-search\tnt4j-streams-k2\config\log4jReplay.properties
 * C:\development\tnt4j\k2\development\k2-index-search\tnt4j-streams-k2
 * 
 * 
 * com.jkoolcloud.tnt4j.streams.StreamsAgent -f:tnt-data-source.xml
 * C:\development\tnt4j\build\tnt4j-streams\tnt4j-streams-1.11.0-SNAPSHOT\bin\..\config\tnt4j.properties
 * file:C:\development\tnt4j\build\tnt4j-streams\tnt4j-streams-1.11.0-SNAPSHOT\bin\..\config\log4j.properties
 * C:\development\tnt4j\build\tnt4j-streams\tnt4j-streams-1.11.0-SNAPSHOT\samples\angular-js-tracing
 * 
 */