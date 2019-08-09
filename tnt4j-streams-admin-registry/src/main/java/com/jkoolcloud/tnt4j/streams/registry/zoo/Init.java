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

package com.jkoolcloud.tnt4j.streams.registry.zoo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.StreamsAgent;
import com.jkoolcloud.tnt4j.streams.registry.zoo.authentication.TokenAuth;
import com.jkoolcloud.tnt4j.streams.registry.zoo.configuration.JettyConfigProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.customJettyLogger;
import com.jkoolcloud.tnt4j.streams.registry.zoo.stats.StreamControls;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.ValidatorUtils;

/**
 * The type Init.
 */
public class Init {

	private static AtomicBoolean hasStreamsAdminStarted = new AtomicBoolean(false);
	public static TokenAuth tokenAuth = new TokenAuth();
	private static JettyConfigProvider jettyConfigProvider = new JettyConfigProvider(
			System.getProperty("streamsAdmin"));

	private ServerConnector setupSsl(Server server) {

		String keyStorePath = jettyConfigProvider.getKeyStorePath();
		String keyStorePassword = jettyConfigProvider.getKeyStorePassword();
		String KeyManagerPassword = jettyConfigProvider.getKeyManagerPassword();
		int jettySecurePort = Integer.parseInt(jettyConfigProvider.getJettySecurePort());

		SslContextFactory sslContextFactory = new SslContextFactory.Server();
		sslContextFactory.setKeyStorePath(keyStorePath);
		sslContextFactory.setKeyStorePassword(keyStorePassword);
		sslContextFactory.setKeyManagerPassword(KeyManagerPassword);

		HttpConfiguration httpsConfig = new HttpConfiguration();

		httpsConfig.setSecureScheme("https");
		httpsConfig.setSecurePort(jettySecurePort);
		httpsConfig.setOutputBufferSize(32768);

		SecureRequestCustomizer src = new SecureRequestCustomizer();
		src.setStsMaxAge(2000);
		src.setStsIncludeSubDomains(true);

		httpsConfig.addCustomizer(src);

		ServerConnector https = new ServerConnector(server,
				new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
				new HttpConnectionFactory(httpsConfig));
		https.setPort(jettySecurePort);
		https.setIdleTimeout(500000);

		return https;
	}

	private void startJetty(Properties properties) {

		Server server = new Server();

		ServletContextHandler servletHandler = new ServletContextHandler();

		servletHandler.addServlet(new ServletHolder(HttpServletDispatcher.class), "/");
		servletHandler.setInitParameter("javax.ws.rs.Application",
				"com.jkoolcloud.tnt4j.streams.registry.zoo.rest.RestScanner");

		servletHandler.setInitParameter("resteasy.providers",
				"com.jkoolcloud.tnt4j.streams.registry.zoo.authentication.RequestFilter");

		HandlerCollection handlers = new HandlerCollection();
		handlers.setHandlers(new Handler[] { servletHandler, new DefaultHandler() });

		server.setHandler(handlers);
		server.setConnectors(new Connector[] { setupSsl(server) });

		server.setRequestLog(new customJettyLogger());

		try {
			server.start();
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	public void createZkTree(Properties properties) {
		String[] nodeList = properties.getProperty("nodeList").split(",");

		for (String node : nodeList) {

			String nodeDescription = properties.getProperty(node);

			String[] nodePathAndEndpoint = nodeDescription.split(";");

			// nodePathAndEndpoint must always contain a path, but an rest endpoint is optional
			// 0 = zk path, 1 = rest endpoint
			if (nodePathAndEndpoint.length == 2) {
				if (!CuratorUtils.doesNodeExist(nodePathAndEndpoint[0])) {
					CuratorUtils.createNode(nodePathAndEndpoint[0]);
					CuratorUtils.setData(nodePathAndEndpoint[0], nodePathAndEndpoint[1]);
				} else if (CuratorUtils.doesNodeExist(nodePathAndEndpoint[0])) {
					CuratorUtils.setData(nodePathAndEndpoint[0], nodePathAndEndpoint[1]);
				}
			} else {
				if (!CuratorUtils.doesNodeExist(nodePathAndEndpoint[0])) {
					CuratorUtils.createNode(nodePathAndEndpoint[0]);
				} else if (node.equals("authToken") && CuratorUtils.doesNodeExist(nodePathAndEndpoint[0])) {
					CuratorUtils.setData(nodePathAndEndpoint[0], tokenAuth.getIdentifier());
				}
			}
		}
	}

	private void publishStreams(Properties properties) {

		String[] streamNodeNames = properties.getProperty("streamsNodeList").split(",");

		Collection<String> streams = StreamsAgent.getRunningStreamNames();

		for (String stream : streams) {
			Map<String, Object> placeholders = new HashMap<>();
			placeholders.put("streamName", stream);

			for (String streamNodeName : streamNodeNames) {
				String templateNodePath = properties.getProperty(streamNodeName);

				String resolvedNodePath = StringSubstitutor.replace(templateNodePath, placeholders);

				String[] nodePathAndEndpoint = resolvedNodePath.split(";");

				// nodePathAndEndpoint must always contain a path, but an rest endpoint is optional
				// 0 = zk path, 1 = rest endpoint
				if (nodePathAndEndpoint.length == 2) {
					if (!CuratorUtils.doesNodeExist(nodePathAndEndpoint[0])) {
						CuratorUtils.createNode(nodePathAndEndpoint[0]);
						CuratorUtils.setData(nodePathAndEndpoint[0], nodePathAndEndpoint[1]);
					} else if (CuratorUtils.doesNodeExist(nodePathAndEndpoint[0])) {
						CuratorUtils.setData(nodePathAndEndpoint[0], nodePathAndEndpoint[1]);
					}
				} else {
					if (!CuratorUtils.doesNodeExist(nodePathAndEndpoint[0])) {
						CuratorUtils.createNode(nodePathAndEndpoint[0]);
					}
				}
			}
		}
	}

	private void startDaemons(Properties properties) {
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = Executors.defaultThreadFactory().newThread(r);
				t.setDaemon(true);
				return t;
			}
		});

		executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				publishStreams(properties);
			}
		}, 1000, 5000, TimeUnit.MILLISECONDS);

		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					String dirToMonitor = properties.getProperty("userRequestsPath");
					if (ValidatorUtils.isResourceAvailable(dirToMonitor, ValidatorUtils.Resource.DIRECTORY)) {
						LoggerWrapper.addMessage(OpLevel.INFO, "starting dir streaming");
						StreamControls.dirStreaming(dirToMonitor);
					}
				} catch (Exception e) {

					LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
				}
			}
		});
	}

	public Init() {
		if (hasStreamsAdminStarted.get()) {
			return;
		}
		hasStreamsAdminStarted.set(true);

		LoggerWrapper.addMessage(OpLevel.INFO, "Starting");

		Properties properties = FileUtils.getProperties(System.getProperty("streamsAdmin"));

		createZkTree(properties);
		startDaemons(properties);
		startJetty(properties);

	}
}
