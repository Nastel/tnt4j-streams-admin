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
import java.util.*;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.log4j.Logger;
import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth.LoginCache;
import com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth.UsersUtils;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;
import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.ZookeeperAccessService;

/**
 * The type Curator utils.
 */
public class CuratorUtils {

	private static Logger LOG = Logger.getLogger(CuratorUtils.class);

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

	public static Collection<String> nodeChildrenList(String path, CuratorFramework curator) {
		ServiceDiscovery<String> serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(curator)
				.basePath(path).build();
		try {
			serviceDiscovery.start();
			Collection<String> serviceNames = serviceDiscovery.queryForNames();
			return serviceNames;
		} catch (Exception e) {
			LOG.info("Problem on getting children nodes for: " + path);
		} finally {
			try {
				Objects.requireNonNull(serviceDiscovery).close();
			} catch (Exception ignored) {
			}
		}
		return Collections.singleton("");
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

	/**
	 * Check if user that has logged in has admin rights and can control other users
	 * 
	 * @param curator
	 * @param nodePath
	 * @param loginData
	 * @return
	 */
	public static Boolean checkIfUserIsAdmin(CuratorFramework curator, String nodePath, String loginData) {
		try {
			List<ACL> aclList = curator.getACL().forPath(nodePath);
			String userId;
			int permission;
			for (ACL acl : aclList) {
				userId = acl.getId().getId();
				String credentials = userId.split(":")[0];// acl.getId().getScheme()+":"+userId;
				permission = acl.getPerms();
				// LOG.info("USER IS ADMIN ------>" + permission +" credentials: "+ loginData.split(":")[0] +"
				// "+credentials);
				if (credentials.equals(loginData.split(":")[0]) && permission >= 16) {
					userMapForAdmin(curator, nodePath);
					return true;
				}
			}
			userMapForUser(curator, nodePath, loginData.split(":")[0]);
		} catch (Exception e) {
			LOG.error("Problem while checking if User is admin on login: " + loginData);
		}
		LOG.info("USER IS ADMIN FALSE ------>");
		return false;
	}

	public static void userMapForAdmin(CuratorFramework curator, String nodePath) {
		LoginCache cache = new LoginCache();
		UsersUtils utils = new UsersUtils();
		int userCount = cache.getUserCount();
		try {
			List<ACL> aclList = curator.getACL().forPath(nodePath);
			for (ACL acl : aclList) {
				LOG.info("USERS ACL data ------> " + acl);
				HashMap usersData = new HashMap<String, String>();
				userCount++;
				String userId = acl.getId().getId().split(":")[0];
				boolean userHasActionRights = checkIfUserHasActionRights(curator, nodePath, userId);
				int permissions = acl.getPerms();
				if (!utils.checkTheUserForExclude(userId)) {
					usersData.put("username", userId);
					usersData.put("cluster", nodePath);
					usersData.put("rights", permissions);
					// LOG.info("DOES user has action rights: "+userHasActionRights);
					usersData.put("action", userHasActionRights);
					cache.setUserMap(userCount, usersData);
				}
			}
			cache.setUserCount(userCount);
		} catch (Exception e) {
			LOG.error("Problem while trying to add user list to admin for cluster: " + nodePath);
			LOG.error(e);
		}
	}

	public static void userMapForUser(CuratorFramework curator, String nodePath, String username) {
		LoginCache cache = new LoginCache();
		UsersUtils utils = new UsersUtils();
		int userCount = cache.getUserCount();
		try {
			ACL acl = utils.getAclListForUser(nodePath, username);
			LOG.info("Simple users ACL: " + acl);
			HashMap usersData = new HashMap<String, String>();
			userCount++;
			boolean userHasActionRights = checkIfUserHasActionRights(curator, nodePath, username);
			int permissions = acl.getPerms();
			if (!utils.checkTheUserForExclude(username)) {
				usersData.put("username", username);
				usersData.put("cluster", nodePath);
				usersData.put("rights", permissions);
				usersData.put("action", userHasActionRights);
				cache.setUserMap(userCount, usersData);
			}
			cache.setUserCount(userCount);
		} catch (Exception e) {
			LOG.error("Problem while trying to add user list to admin for cluster: " + nodePath);
			LOG.error(e);
		}
	}

	public static Boolean checkIfUserHasActionRights(CuratorFramework curator, String nodePath, String loginData) {
		String actionNodePath = "";
		List<String> nodes = null;
		ZookeeperAccessService zooAccess = new ZookeeperAccessService();
		try {
			actionNodePath = PropertyData.getProperty("authorizationTokenAction");
			nodes = ZKUtil.listSubTreeBFS(curator.getZookeeperClient().getZooKeeper(), nodePath);
			for (String node : nodes) {
				// LOG.info("node path "+ node);
				// LOG.info("node address ending "+ zooAccess.getAddressEnding(node));
				// LOG.info("action node path "+ actionNodePath);
				if (zooAccess.getAddressEnding(node).equals(actionNodePath)) {
					try {
						String userId;
						List<ACL> aclList = curator.getACL().forPath(node);
						for (ACL acl : aclList) {
							userId = acl.getId().getId();
							String credentials = userId.split(":")[0];
							if (credentials.equals(loginData.split(":")[0])) {
								LOG.info("THE USER REALLY HAS ADMIN RIGHTS" + node + " : " + credentials);
								return true;
							}
						}
					} catch (Exception e) {
						// LOG.error("No node access for the user return false");
						return false;
					}
				}
			}
		} catch (Exception e) {
			// LOG.error("Problem while checking if user has action rights on login");
			return false;
		}
		return false;
	}

}
