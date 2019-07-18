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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.JsonRpcGeneric;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StaticObjectMapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.*;

/**
 * The type Init.
 */
public class Init {

	private Properties getProperties(String path) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(path));
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
		return properties;
	}

	private void configureDistributedQueue(CuratorFramework curatorFramework, String path) {
		DistributedQueueManagerSingleton.Init(curatorFramework, path);
	}

	private void configureAndStartRequestListener(CuratorFramework curatorFramework, String path, Boolean cacheData) {
		PathCacheManagerSingleton.Init(curatorFramework, path, cacheData);
		ZookeeperRequestProcessor zkRequestProcessor = new ZookeeperRequestProcessor();

		PathCacheManagerSingleton.getPathCacheManager().addListenerToPath(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {

				switch (event.getType()) {
				case CHILD_ADDED:
					LoggerWrapper.addMessage(OpLevel.INFO, "Received request");

					byte[] bytes = DistributedQueueManagerSingleton.getDistributedQueueManager().consume();
					JsonRpcGeneric jsonRpc = StaticObjectMapper.mapper.readValue(bytes, JsonRpcGeneric.class);

					zkRequestProcessor.methodSelector(jsonRpc);

					PathCacheManagerSingleton.getPathCacheManager().clear();
					break;
				default:
					break;
				}

			}
		});

		try {
			PathCacheManagerSingleton.getPathCacheManager().startPathCacheListener();
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	/**
	 * Start.
	 */
	private void configureAndStartScheduler() {
		SchedulerFactory factory = null;
		try {
			factory = new StdSchedulerFactory(System.getProperty("quartz"));
		} catch (SchedulerException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
			LoggerWrapper.addMessage(OpLevel.INFO, String.format("CWD: %s", new File("./").getAbsolutePath()));
		}
		Scheduler scheduler = null;

		try {
			scheduler = factory.getScheduler();
		} catch (SchedulerException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		try {
			scheduler.start();
		} catch (SchedulerException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	/**
	 * Init.
	 */
	public Init() {
		String path = System.getProperty("zkTree");

		Properties properties = getProperties(path);

		String connectString = properties.getProperty("connectString");
		int baseSleepTimeMs = Integer.parseInt(properties.getProperty("baseSleepTimeMs"));
		int maxRetries = Integer.parseInt(properties.getProperty("maxRetries"));

		CuratorSingleton.init(connectString, baseSleepTimeMs, maxRetries);
		CuratorSingleton.getSynchronizedCurator().start();

		ZkTree.setProperties(properties);

		String pathToAgent = ZkTree.createZkTree(CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
		ZkTree.registerStreams(pathToAgent, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());

		configureDistributedQueue(CuratorSingleton.getSynchronizedCurator().getCuratorFramework(),
				properties.getProperty("requestNode"));
		configureAndStartRequestListener(CuratorSingleton.getSynchronizedCurator().getCuratorFramework(),
				properties.getProperty("requestNode"), false);

		configureAndStartScheduler();
	}
}
