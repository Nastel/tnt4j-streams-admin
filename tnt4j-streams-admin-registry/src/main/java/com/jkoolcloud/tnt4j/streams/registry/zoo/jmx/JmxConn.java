package com.jkoolcloud.tnt4j.streams.registry.zoo.jmx;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.utils.LoggerUtils;

public class JmxConn {

	private JMXServiceURL serviceUrl;
	private JMXConnector jmxConnector;
	private MBeanServerConnection mbeanConn;
	private JmxStatistics jmxStatistics;

	private static final EventSink JmxConnEventSink = LoggerUtils.getLoggerSink("JmxConn");

	public JmxConn(String host, int port) {
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
			JmxConnEventSink.log(OpLevel.ERROR, "Malformed connecting string for JMXServiceURL", e);
		}

		try {
			jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
		} catch (IOException e) {
			JmxConnEventSink.log(OpLevel.ERROR, "Could not connect to the machine with given JMXServiceURL", e);
		}

		try {
			mbeanConn = jmxConnector.getMBeanServerConnection();
		} catch (IOException e) {
			JmxConnEventSink.log(OpLevel.ERROR, "Could not connect to MBeanServer with given jmxConnector", e);
		}
	}

}
