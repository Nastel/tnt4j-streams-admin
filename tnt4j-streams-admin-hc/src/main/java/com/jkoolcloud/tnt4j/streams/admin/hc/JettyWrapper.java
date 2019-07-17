/*
 * Copyright 2014-2019 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.streams.admin.hc;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.codahale.metrics.servlets.PingServlet;
import com.codahale.metrics.servlets.ThreadDumpServlet;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.admin.utils.log.ErrorLogsServlet;
import com.jkoolcloud.tnt4j.streams.admin.utils.log.LogsServlet;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStreamStatistics;
import com.jkoolcloud.tnt4j.streams.utils.LoggerUtils;

/**
 * The type Jetty wrapper.
 */
public class JettyWrapper {
	private static final EventSink LOGGER = LoggerUtils.getLoggerSink(JettyWrapper.class);

	private static Server server = null;
	// private static ZookeeperManager zookeeperManager = null;

	/**
	 * Instantiates a new Jetty wrapper.
	 */
	public JettyWrapper() {
	}

	/**
	 * Prepare jetty.
	 *
	 * @param port
	 *            the port
	 * @param registry
	 *            the registry
	 *
	 * @throws Exception
	 *             the exception
	 */
	public static void prepareJetty(int port, HealthCheckRegistry registry) throws Exception {
		server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);

		server.setConnectors(new Connector[] { connector });

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(HealthCheckServlet.class, "/health");
		context.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, registry);
		context.addServlet(MetricsServlet.class, "/metrics");
		context.setAttribute(MetricsServlet.METRICS_REGISTRY, TNTInputStreamStatistics.getMetrics());
		context.addServlet(PingServlet.class, "/ping");
		context.addServlet(ThreadDumpServlet.class, "/threadDump");
		context.addServlet(LogsServlet.class, "/logs");
		context.addServlet(ErrorLogsServlet.class, "/logsErr");
		context.setSessionHandler(new SessionHandler());

		HandlerCollection handlers = new HandlerCollection();
		handlers.setHandlers(new Handler[] { context, new DefaultHandler() });
		server.setHandler(handlers);
		server.setStopAtShutdown(true);
		server.setStopTimeout(TimeUnit.SECONDS.toMillis(20));

		// instantiateZookeeperManager();

		server.start();
		// server.join();
	}

	/*
	private static void instantiateZookeeperManager() {
		String serviceProperties = System.getProperty("service");
		if (StringUtils.isNotEmpty(serviceProperties)) {
			Properties properties = FileUtils.loadPropertiesWrapper(serviceProperties);
			zookeeperManager = new ZookeeperManager(properties);

			zookeeperManager.startZookeeperManager();
			zookeeperManager.registerAndStartServices();
		}
	}
	 */

	/**
	 * Shutdown jetty.
	 */
	public static void shutdownJetty() {
		// if (zookeeperManager != null) {
		// zookeeperManager.shutdownZookeeperManager();
		// }

		if (server != null) {
			try {
				server.stop();
			} catch (Exception exc) {
				LOGGER.log(OpLevel.ERROR, "Failed to stop Jetty server: {0}\n\t", exc);
			} finally {
				server.destroy();
			}
		}
	}

}
