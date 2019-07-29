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

package com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.ZookeeperAccessService;

/**
 * The type Curator utils.
 */
public class CuratorUtils {

	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperAccessService.class);

	/**
	 * Does node exist boolean.
	 *
	 * @param path
	 *            the path
	 * @param curator
	 *            the curator
	 * @return the boolean
	 */
	public static boolean doesNodeExist(String path, CuratorFramework curator) {
		Stat stat = null;
		try {
			stat = curator.checkExists().forPath(path);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return stat == null ? false : true;
	}

	/**
	 * Create node.
	 *
	 * @param path
	 *            the path
	 * @param curator
	 *            the curator
	 */
	public static void createNode(String path, CuratorFramework curator) {
		try {
			String result = curator.create().forPath(path);
			// System.out.println("Node created: " + result);

		} catch (Exception e) {
			LOG.error("Node with path: " + path + " was not created successfully");
			e.printStackTrace();
		}
	}

	/**
	 * Sets dataReading.
	 *
	 * @param path
	 *            the path
	 * @param data
	 *            the dataReading
	 * @param curator
	 *            the curator
	 */
	public static void setData(String path, String data, CuratorFramework curator) {
		try {
			Stat stat = curator.setData().forPath(path, data.getBytes());
			// System.out.println("Node updated: " + stat.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delete node.
	 *
	 * @param path
	 *            the path
	 * @param curator
	 *            the curator
	 */
	public static void deleteNode(String path, CuratorFramework curator) {
		try {
			curator.delete().forPath(path);
		} catch (Exception e) {
			LOG.error("Node with path: " + path + " was not deleted successfully");
			e.printStackTrace();
		}
	}

	/**
	 * Get dataReading byte [ ].
	 *
	 * @param path
	 *            the path
	 * @param curator
	 *            the curator
	 * @return the byte [ ]
	 */
	public static byte[] getData(String path, CuratorFramework curator) {
		byte[] bytes = null;
		try {
			bytes = curator.getData().forPath(path);
		} catch (Exception e) {
			e.printStackTrace();
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
	 * @param curator
	 *            the curator
	 */
	public static void getOfferedServicePath(String service, String offeredServicesPath, CuratorFramework curator) {
		ServiceDiscovery<String> serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(curator)
				.basePath(offeredServicesPath).build();
		try {
			serviceDiscovery.start();
			Collection<String> serviceNames = serviceDiscovery.queryForNames();
			// System.out.println("Services found:" + serviceNames );

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create head string.
	 *
	 * @param head
	 *            the head
	 * @param curator
	 *            the curator
	 * @return the string
	 */
	public static String createHead(String head, CuratorFramework curator) {
		if (head.startsWith("/")) {
			CuratorUtils.createNode(head, curator);
			return head;
		} else {
			String headPath = "/" + head;
			CuratorUtils.createNode(headPath, curator);
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
	 * @param curator
	 *            the curator
	 * @return the string
	 */
	public static String createChild(String path, String name, CuratorFramework curator) {
		if (path.endsWith("/")) {
			CuratorUtils.createNode(path + name, curator);
			return path;
		} else {
			String childPath = path + "/" + name;
			CuratorUtils.createNode(childPath, curator);
			return childPath;
		}
	}

	/**
	 * Create default node hierarchy.
	 *
	 * @param curator
	 *            the curator
	 */
	public static void createDefaultNodeHierarchy(CuratorFramework curator) {

		if (doesNodeExist("/streams2", curator)) {
			return;
		}

		String headPath = createHead("/streams2", curator);
		String childPath = createChild(headPath, "v1", curator);
		String activeServicesPath = createChild(childPath, "activeServices", curator);
		String activeServicesEth = createChild(activeServicesPath, "EthereumStreamServices", curator);
		createChild(activeServicesEth, "instances", curator);
		createChild(activeServicesEth, "request", curator);
		createChild(activeServicesEth, "responses", curator);
		String offeredServices = createChild(childPath, "offeredServices", curator);
		String offeredServiceEthStreamService = createChild(offeredServices, "EthereumStreamServices", curator);

		String ethereumStreamServicesProps = null;
		try {
			ethereumStreamServicesProps = readFile(System.getProperty("ethereum.config"), Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}

		setData(offeredServiceEthStreamService, ethereumStreamServicesProps, curator);

	}

	/**
	 * Read file string.
	 *
	 * @param path
	 *            the path
	 * @param encoding
	 *            the encoding
	 * @return the string
	 * @throws IOException
	 *             the io exception
	 */
	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

}
