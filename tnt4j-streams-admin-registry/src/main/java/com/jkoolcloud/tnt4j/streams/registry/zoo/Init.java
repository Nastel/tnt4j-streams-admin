package com.jkoolcloud.tnt4j.streams.registry.zoo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.xml.sax.SAXException;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.authentication.TokenAuth;
import com.jkoolcloud.tnt4j.streams.registry.zoo.configuration.*;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.CustomJettyLogger;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.stats.StreamControls;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JsonUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.ValidatorUtils;

public class Init {

	private static Root root;

	private static Zookeeper zookeeper;
	private static Jetty jetty;
	private static Paths paths;
	private static Variables variables;

	private static final TokenAuth readToken = new TokenAuth();
	private static final TokenAuth actionToken = new TokenAuth();

	private static Server server = new Server();
	private static CuratorWrapper curatorWrapper;

	private static AtomicBoolean streamsAdminRunning = new AtomicBoolean(false);

	private ExecutorService streamAdminMainThread = Executors.newSingleThreadExecutor();

	public static TokenAuth getReadToken() {
		return readToken;
	}

	public static TokenAuth getActionToken() {
		return actionToken;
	}

	public static Zookeeper getZookeeper() {
		return zookeeper;
	}

	public static Jetty getJetty() {
		return jetty;
	}

	public static Paths getPaths() {
		return paths;
	}

	private String replacePlaceholders(String content) {
		return StringSubstitutor.replace(content, variables.getAdditionalProperties());
	}

	private void createNode(String path, String data) {
		data = replacePlaceholders(data);
		path = replacePlaceholders(path);
		if (curatorWrapper.doesNodeExist(path)) {
			curatorWrapper.setData(path, data);
		} else {
			curatorWrapper.createNode(path);
			curatorWrapper.setData(path, data);
		}

		if (path.contains("_actionToken")) {
			curatorWrapper.setData(path, actionToken.getIdentifier());
		} else if (path.contains("_readToken")) {
			curatorWrapper.setData(path, readToken.getIdentifier());
		}
	}

	private void createBaseTree(List<AgentNode> agentNodeList) {
		for (AgentNode agentNode : agentNodeList) {

			String path = agentNode.getPath();
			String data = JsonUtils.objectToString(agentNode.getAdditionalProperties());

			createNode(path, data);
		}
	}

	private void createStreamNodes(List<StreamNode> streamNodeList) {

		Set<String> streams = null;
		try {
			streams = FileUtils.getStreamsAndClasses(paths.getMainConfigPath()).keySet();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		for (StreamNode streamNode : streamNodeList) {
			String template = streamNode.getPath();

			Map<String, Object> subs = new HashMap<>();
			for (String stream : streams) {
				subs.put("streamName", stream);

				String path = StringSubstitutor.replace(template, subs);

				String data = StringSubstitutor.replace(JsonUtils.objectToString(streamNode.getAdditionalProperties()),
						subs);

				createNode(path, data);
			}
		}
	}

	private ServerConnector setupSsl(Server server) {

		String keyStorePath = jetty.getKeyStorePath();
		String keyStorePassword = jetty.getKeyStorePassword();
		String KeyManagerPassword = jetty.getKeyManagerPassword();
		int jettySecurePort = jetty.getSecurePort();

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

	private void configureConnectors() {

		ServletContextHandler servletHandler = new ServletContextHandler();

		servletHandler.addServlet(new ServletHolder(HttpServletDispatcher.class), "/");
		servletHandler.setInitParameter("javax.ws.rs.Application",
				"com.jkoolcloud.tnt4j.streams.registry.zoo.rest.RestScanner");

		servletHandler.setInitParameter("resteasy.providers",
				"com.jkoolcloud.tnt4j.streams.registry.zoo.authentication.ReadRequestFilter,"
						+ "com.jkoolcloud.tnt4j.streams.registry.zoo.authentication.ActionRequestFilter");

		HandlerCollection handlers = new HandlerCollection();
		handlers.setHandlers(new Handler[] { servletHandler, new DefaultHandler() });

		server.setHandler(handlers);
		server.setConnectors(new Connector[] { setupSsl(server) });

		server.setRequestLog(new CustomJettyLogger());
	}

	private void startJetty() {
		try {
			server.start();
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	private void stopJetty() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
			} finally {
				server.destroy();
			}
		}
	}

	private void dirStreaming(String dir) {

		if (dir == null || dir.isEmpty()) {
			LoggerWrapper.addMessage(OpLevel.INFO, "Dir streaming is off");
			return;
		}

		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = Executors.defaultThreadFactory().newThread(r);
				t.setDaemon(true);
				return t;
			}
		});

		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					if (ValidatorUtils.isResourceAvailable(dir, ValidatorUtils.Resource.DIRECTORY)) {
						LoggerWrapper.addMessage(OpLevel.INFO, "starting dir streaming");
						StreamControls.dirStreaming(dir);
					}
				} catch (Exception e) {
					LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
				}
			}
		});
	}

	public void loadConfigAndStartCurator(String zkPath) {
		root = JsonUtils.jsonToObject(zkPath, Root.class);

		zookeeper = root.getZookeeper();
		jetty = root.getJetty();
		paths = root.getPaths();
		variables = root.getVariables();

		String connectString = zookeeper.getConnectString();
		String zklogin = zookeeper.getZklogin();
		String zkpass = zookeeper.getZkpass();

		curatorWrapper = new CuratorWrapper(zklogin, zkpass, connectString);
		curatorWrapper.start();
	}

	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				closeStreamsAdmin();
			}
		});
	}

	private void closeStreamsAdmin() {
		if (curatorWrapper != null) {
			curatorWrapper.close();
		}
		stopJetty();

		try {
			LoggerWrapper.closeLogger();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			streamAdminMainThread.shutdown();
			streamAdminMainThread.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			streamAdminMainThread.shutdownNow();
		}
	}

	private void startStreamAdmin(String zkPath) {
		registerShutdownHook();
		loadConfigAndStartCurator(zkPath);
		createBaseTree(zookeeper.getAgentNodes());
		createStreamNodes(zookeeper.getStreamNodes());
		dirStreaming(paths.getMonitoredPath());
		configureConnectors();
		startJetty();
		LoggerWrapper.addMessage(OpLevel.INFO, "Stream admin has started successfully");

	}

	public Init(String zkPath) {
		if (!streamsAdminRunning.get()) {
			streamsAdminRunning.set(true);
			streamAdminMainThread.submit(new Runnable() {
				@Override
				public void run() {
					startStreamAdmin(zkPath);
				}
			});
		}
	}
}
