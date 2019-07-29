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

import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.StreamsAgent;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.IoUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;

/**
 * The type Init.
 */
public class Init {

	private void startJetty() {
		Server server = new Server(8899);

		ServletContextHandler servletHandler = new ServletContextHandler();

		servletHandler.addServlet(new ServletHolder(HttpServletDispatcher.class), "/");
		servletHandler.setInitParameter("javax.ws.rs.Application",
				"com.jkoolcloud.tnt4j.streams.registry.zoo.RestEndpoint.RestScanner");

		HandlerCollection handlers = new HandlerCollection();
		handlers.setHandlers(new Handler[] { servletHandler, new DefaultHandler() });
		server.setHandler(handlers);

		// SslContextFactory sslContextFactory = new SslContextFactory.Server();
		// sslContextFactory.setKeyStorePath("C:\\Users\\Tomaš\\Desktop\\server.cer");
		// sslContextFactory.setKeyStorePassword("123456789");

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

			String[] nodePathAndEndpoint = nodeDescription.split(",");

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

	private void publishStreams(Properties properties) {
		String[] streamNodeNames = properties.getProperty("streamsNodeList").split(",");

		Collection<String> streams = StreamsAgent.getRunningStreamNames();

		for (String stream : streams) {
			Map<String, Object> placeholders = new HashMap<>();
			placeholders.put("streamName", stream);

			for (String streamNodeName : streamNodeNames) {
				String templateNodePath = properties.getProperty(streamNodeName);

				String resolvedNodePath = StringSubstitutor.replace(templateNodePath, placeholders);

				String[] nodePathAndEndpoint = resolvedNodePath.split(",");

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

	private void setStreamPublisherDaemon(Properties properties) {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
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
		}, 1000, 1000, TimeUnit.MILLISECONDS);
	}

	public Init() {

		/*
		 * KeyStore keyStore = null; try { keyStore = KeyStore.getInstance("JKS"); } catch (KeyStoreException e) {
		 * e.printStackTrace(); }
		 * 
		 * Key key = null; try { key = keyStore.getKey("mykey", "".toCharArray()); } catch (KeyStoreException e) {
		 * e.printStackTrace(); } catch (NoSuchAlgorithmException e) { e.printStackTrace(); } catch
		 * (UnrecoverableKeyException e) { e.printStackTrace(); }
		 */

		Properties properties = IoUtils.getProperties(System.getProperty("streamsAdmin"));

		createZkTree(properties);

		setStreamPublisherDaemon(properties);

		startJetty();

	}
}
