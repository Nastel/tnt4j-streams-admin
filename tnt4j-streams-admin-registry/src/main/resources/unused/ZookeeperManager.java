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

package unused;

import static com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils.createDefaultNodeHierarchy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.JsonRpc;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StaticObjectMapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.ZookeeperRequestProcessor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * The type Zookeeper manager.
 */
public class ZookeeperManager {


	private CuratorFramework curatorFramework;
	private ServiceInstanceManager serviceInstanceManager = null;
	private TreeCacheManager treeCacheManager = null;
	private ServiceManager serviceManager = null;
	private Properties serviceProperties = null;
	private TaskManager taskManager = null;

	/**
	 * Instantiates a new Zookeeper manager.
	 *
	 * @param properties
	 *            the properties
	 */
	public ZookeeperManager(Properties properties) {
		curatorFramework = createCuratorInstance(properties);

		serviceInstanceManager = new ServiceInstanceManager(new HashMap<>());
		treeCacheManager = new TreeCacheManager(new HashMap<>());
		serviceManager = new ServiceManager(curatorFramework);
		taskManager = new TaskManager(new HashMap<>());
		serviceProperties = properties;
	}

	private CuratorFramework createCuratorInstance(Properties properties) {
		String zookeeperUrl = properties.getProperty("zookeeperUrl");
		int baseSleepTime = Integer.parseInt(properties.getProperty("baseSleepTime"));
		int maxRetries = Integer.parseInt(properties.getProperty("maxRetries"));

		return CuratorFrameworkFactory.newClient(zookeeperUrl, new ExponentialBackoffRetry(baseSleepTime, maxRetries));
	}

	/**
	 * Start zookeeper manager.
	 */
	public void startZookeeperManager() {
		curatorFramework.start();
	}

	/**
	 * Shutdown zookeeper manager.
	 */
	public void shutdownZookeeperManager() {
		treeCacheManager.closeAllListeners();
		serviceInstanceManager.closeAllServices();
		taskManager.stopAllTimers();

		curatorFramework.close();
	}

	/**
	 * Register service.
	 */
	public void registerService() {
		String offeredServicesPath = serviceProperties.getProperty("registrationPath");
		String serviceName = serviceProperties.getProperty("name");
		String staticDataPath = serviceProperties.getProperty("staticData");

		serviceManager.registerOrUpdateOfferedService(offeredServicesPath, staticDataPath, serviceName);
	}

	/**
	 * Register instance.
	 */
	public void registerInstance() {
		serviceInstanceManager.createService(serviceProperties, curatorFramework);
	}

	/**
	 * Start instance.
	 */
	public void startInstance() {
		serviceInstanceManager.startAllServices();
	}

	/**
	 * Register listener.
	 */
	public void registerListener() {
		Map<String, Object> serviceRequestDir = null;
		try {
			serviceRequestDir = StaticObjectMapper.mapper.readValue(serviceProperties.get("payload").toString(),
					new TypeReference<Map<String, Object>>() {
					});
		} catch (IOException e) {
			e.printStackTrace();
		}

		String requestDir = (String) serviceRequestDir.get("requestDir");
		String responseDir = (String) serviceRequestDir.get("responseDir");

		treeCacheManager.registerTreeCacheForPath(requestDir, curatorFramework);
		TreeCacheListener treeCacheListener = new TreeCacheListener() {
			// *****************************************************************
			// This a temporally and solution should be reworked in the future
			// ******************************************************************
			@Override
			public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
				switch (event.getType()) {
				case NODE_ADDED: {

					// Temporally solution, because tree listener does not always catches NODE_UPDATE events
					Thread.sleep(Integer.parseInt(serviceProperties.getProperty("threadSleepTimeMs")));

					String request = new String(CuratorUtils.getData(event.getData().getPath(), curatorFramework));

					// because by default when a node is created by the curator it also calls setData and sets the
					// creators IP address
					// due to this a NODE_UPDATE event is raised and we avoid this by checking the content length
					// 25 is hardcoded here, to filter out IPv4 from a request, IPv4 max length is 15 and IPv6 45
					if (request.length() > 25) {
						JsonRpc jsonRpcRequest = StaticObjectMapper.mapper.readValue(request, JsonRpc.class);
						String responseDirPath = responseDir;
						String response = responseDirPath + "/" + jsonRpcRequest.getId();
						ZookeeperRequestProcessor zookeeperRequestProcessor = new ZookeeperRequestProcessor(
								curatorFramework, serviceProperties);
						zookeeperRequestProcessor.methodSelector(jsonRpcRequest, response);
					}
					break;
				}
				}
			}
		};
		treeCacheManager.addListenerToPath(requestDir, treeCacheListener);
	}

	/**
	 * Start listener.
	 */
	public void startListener() {
		treeCacheManager.startAllListeners();
	}

	/*
	 * private StringBufferAppender initStringBufferAppender(String appenderName) { StringBufferAppender
	 * stringBufferAppender = null; org.apache.log4j.Logger logger = Logger.getRootLogger(); stringBufferAppender =
	 * (StringBufferAppender) logger.getAppender(appenderName); return stringBufferAppender; }
	 * 
	 * 
	 * private boolean setLogsToZookeeper(List<String> logEntries, String path) { if (logEntries == null) { return
	 * false; }
	 * 
	 * StringBuilder stringBuilder = new StringBuilder(); for (String entry : logEntries) { stringBuilder.append(entry);
	 * }
	 * 
	 * String logs = stringBuilder.toString();
	 * 
	 * boolean wasDataSet = CuratorUtils.setData(path, logs, curatorFramework);
	 * 
	 * return wasDataSet; }
	 * 
	 * 
	 * private void createTask(String appender, String path, long period) { StringBufferAppender stringBufferAppender =
	 * initStringBufferAppender(appender);
	 * 
	 * taskManager.createTimer(appender);
	 * 
	 * TimerTask timerTask = new TimerTask() {
	 * 
	 * @Override public void run() { List<String> logsEntries = stringBufferAppender.getLogs();
	 * setLogsToZookeeper(logsEntries, path); } };
	 * 
	 * 
	 * taskManager.giveTimerTask(appender, timerTask, period); }
	 * 
	 * 
	 * private void CreateAllTasks(){ Map<String, Object> serviceRequestDir = null;
	 * 
	 * try { serviceRequestDir = StaticObjectMapper.mapper.readValue(serviceProperties.get("payload").toString(), new
	 * TypeReference<Map<String, Object>>() { }); } catch (IOException e) { e.printStackTrace(); }
	 * 
	 * String logsDir = (String) serviceRequestDir.get("logsDir"); String errLogsDir = (String)
	 * serviceRequestDir.get("errLogsDir");
	 * 
	 * 
	 * 
	 * 
	 * createTask(serviceProperties.getProperty("logAppender"), logsDir,
	 * Long.parseLong(serviceProperties.getProperty("logsUpdatePeriodMs"), 10)); //
	 * createTask(serviceProperties.getProperty("errLogAppender"), errLogsDir,
	 * Long.parseLong(serviceProperties.getProperty("logsUpdatePeriodMs"), 10)); }
	 * 
	 */

	/**
	 * Register and start services.
	 */
	public void registerAndStartServices() {
		createDefaultNodeHierarchy(curatorFramework);
		registerService();
		registerInstance();
		startInstance();
		registerListener();
		startListener();
		// CreateAllTasks();
	}

}
