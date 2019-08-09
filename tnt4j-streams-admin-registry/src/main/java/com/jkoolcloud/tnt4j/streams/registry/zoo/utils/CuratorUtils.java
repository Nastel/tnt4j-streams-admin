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

package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.PathUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;

/**
 * The type Curator utils.
 */
public class CuratorUtils {

	private static final CuratorFramework curatorFramework;

	static {
		Properties properties = FileUtils.getProperties(System.getProperty("streamsAdmin"));

		String connectString = properties.getProperty("connectString");
		int baseSleepTimeMs = Integer.parseInt(properties.getProperty("baseSleepTimeMs"));
		int maxRetries = Integer.parseInt(properties.getProperty("maxRetries"));

		curatorFramework = CuratorFrameworkFactory.newClient(connectString,
				new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries));

		curatorFramework.start();
	}

	private static boolean validateCuratorParam() {

		if (curatorFramework == null) {
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING, String.format("Curator framework is NULL \n %s", stackTrace));
			return false;
		}

		if (curatorFramework.getState() != CuratorFrameworkState.STARTED) {
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING,
					String.format("Curator framework was not started: \n %s", stackTrace));
			return false;
		}

		return true;
	}

	private static boolean validatePayload(String payload) {
		int MAX_ZK_PAYLOAD_SIZE_BYTES = 1_000_000;
		if (payload == null) {
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING, String.format("Payload is NULL: %s", stackTrace));

			return false;
		}
		if (payload.getBytes().length > MAX_ZK_PAYLOAD_SIZE_BYTES) {
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING, String.format("Invalid parameter payload, too big: %d \n %s",
					payload.getBytes().length, stackTrace));

			return false;
		}

		return true;
	}

	private static boolean validatePath(String path) {
		if (path == null) {
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING, String.format("Path is NULL \n %s", stackTrace));

			return false;
		}

		boolean isPathValid = true;

		try {
			PathUtils.validatePath(path);
		} catch (IllegalArgumentException e) {
			isPathValid = false;
		}

		if (!isPathValid || !doesNodeExist(path)) {
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING,
					String.format("Invalid parameter path: %s \n %s", path, stackTrace));

			return false;
		}
		return true;
	}

	private static boolean areParameterValid(String path, String payload) {

		if (!validateCuratorParam()) {
			return false;
		}
		if (!validatePayload(payload)) {
			return false;
		}
		return validatePath(path);
	}

	/**
	 * Does node exist boolean.
	 *
	 * @param path
	 *            the path
	 * @return the boolean
	 */
	public static boolean doesNodeExist(String path) {
		Stat stat = null;
		try {
			stat = curatorFramework.checkExists().forPath(path);
		} catch (Exception e) {
			System.out.println(path);
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		return stat == null ? false : true;
	}

	/**
	 * Create node.
	 *
	 * @param path
	 *            the path
	 */
	public static void createNode(String path) {
		try {
			if (!doesNodeExist(path)) {
				String result = curatorFramework.create().forPath(path);
			}
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	/**
	 * Sets data.
	 *
	 * @param path
	 *            the path
	 * @param data
	 *            the data
	 * @return the data
	 */
	public static boolean setData(String path, String data) {
		Stat stat = null;
		try {
			if (areParameterValid(path, data)) {
				stat = curatorFramework.setData().forPath(path, data.getBytes());
			}
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
		return stat != null;
	}

	/**
	 * Sets data.
	 *
	 * @param path
	 *            the path
	 * @param data
	 *            the data
	 * @return the data
	 */
	public static boolean CheckIfNodeExistsAndSetData(String path, String data) {
		Stat stat = null;
		try {
			if (doesNodeExist(path)) {
				stat = curatorFramework.setData().forPath(path, data.getBytes());
			}
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
		return stat != null;
	}

	/**
	 * Delete node.
	 *
	 * @param path
	 *            the path
	 */
	public static void deleteNode(String path) {
		try {
			curatorFramework.delete().forPath(path);
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	/**
	 * Get data byte [ ].
	 *
	 * @param path
	 *            the path
	 * @return the byte [ ]
	 */
	public static byte[] getData(String path) {
		byte[] bytes = null;
		try {
			bytes = curatorFramework.getData().forPath(path);
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
		return bytes;
	}

	/**
	 * Gets offered service path.
	 *
	 * @param service
	 *            the service
	 * @param offeredServicesPath
	 *            the offered services path
	 */
	public static void getOfferedServicePath(String service, String offeredServicesPath) {
		ServiceDiscovery<String> serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class)
				.client(curatorFramework).basePath(offeredServicesPath).build();
		try {
			serviceDiscovery.start();
			Collection<String> serviceNames = serviceDiscovery.queryForNames();

		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	/**
	 * Create head string.
	 *
	 * @param head
	 *            the head
	 * @return the string
	 */
	public static String createHead(String head) {
		if (head.startsWith("/")) {
			CuratorUtils.createNode(head);
			return head;
		} else {
			String headPath = "/" + head;
			CuratorUtils.createNode(headPath);
			return headPath;
		}
	}

	/**
	 * Create child string.
	 *
	 * @param path
	 *            the path
	 * @param name
	 *            the name
	 * @return the string
	 */
	public static String createChild(String path, String name) {
		if (path.endsWith("/")) {
			CuratorUtils.createNode(path + name);
			return path;
		} else {
			String childPath = path + "/" + name;
			CuratorUtils.createNode(childPath);
			return childPath;
		}
	}

	public static void createEphemeralNode(String path, CuratorFramework curator) {
		try {
			if (!doesNodeExist(path)) {
				String result = curator.create().withMode(CreateMode.EPHEMERAL).forPath(path);
			}
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}
}
