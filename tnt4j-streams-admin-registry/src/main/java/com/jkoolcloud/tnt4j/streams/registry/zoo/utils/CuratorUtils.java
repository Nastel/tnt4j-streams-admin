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

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.utils.PathUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

/**
 * The type Curator utils.
 */
public class CuratorUtils {


	private static boolean validateCuratorParam(CuratorFramework curatorFramework){
		if(curatorFramework == null  ){
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING, String.format("Curator framework is NULL \n %s", stackTrace ));
			return false;
		}

		if(curatorFramework.getState() !=  CuratorFrameworkState.STARTED ){
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING, String.format("Curator framework was not started: \n %s", stackTrace ));
			return false;
		}

		return true;
	}


	private static boolean validatePayload(String payload){
		int MAX_ZK_PAYLOAD_SIZE_BYTES = 1_000_000;
		if(payload == null ){
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING, String.format("Payload is NULL: %s", stackTrace ));

			return false;
		}
		if(payload.getBytes().length > MAX_ZK_PAYLOAD_SIZE_BYTES){
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING, String.format("Invalid parameter payload, too big: %d \n %s", payload.getBytes().length, stackTrace ));

			return false;
		}

		return true;
	}

	private static boolean validatePath(String path, CuratorFramework curatorFramework){
		if(path == null){
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING, String.format("Path is NULL \n %s", stackTrace ));

			return false;
		}

		boolean isPathValid = true;

		try {
			PathUtils.validatePath(path);
		}catch (IllegalArgumentException e){
			isPathValid = false;
		}

		if( !isPathValid || !doesNodeExist(path, curatorFramework)){
			String stackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
			LoggerWrapper.addMessage(OpLevel.WARNING, String.format("Invalid parameter path: %s \n %s", path, stackTrace ));

			return false;
		}
		return true;
	}


	private static boolean areParameterValid(CuratorFramework curatorFramework, String path, String payload){

		if(!validateCuratorParam(curatorFramework)){
			return false;
		}
		if(!validatePayload(payload)){
			return false;
		}
		if(!validatePath(path, curatorFramework)){
			return false;
		}

		return true;
	}


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
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
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
			if (!doesNodeExist(path, curator)) {
				String result = curator.create().forPath(path);
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
	 * @param curator
	 *            the curator
	 * @return the data
	 */
	public static boolean setData(String path, String data, CuratorFramework curator) {
		Stat stat = null;
		try {
			if (areParameterValid(curator, path, data)) {
				stat = curator.setData().forPath(path, data.getBytes());
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
	 * @param curator
	 *            the curator
	 * @return the data
	 */
	public static boolean CheckIfNodeExistsAndSetData(String path, String data, CuratorFramework curator) {
		Stat stat = null;
		try {
			if (doesNodeExist(path, curator)) {
				stat = curator.setData().forPath(path, data.getBytes());
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
	 * @param curator
	 *            the curator
	 */
	public static void deleteNode(String path, CuratorFramework curator) {
		try {
			curator.delete().forPath(path);
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	/**
	 * Get data byte [ ].
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
	 * @param curator
	 *            the curator
	 */
	public static void getOfferedServicePath(String service, String offeredServicesPath, CuratorFramework curator) {
		ServiceDiscovery<String> serviceDiscovery = ServiceDiscoveryBuilder.builder(String.class).client(curator)
				.basePath(offeredServicesPath).build();
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


	public static void createEphemeralNode(String path, CuratorFramework curator ){
		try {
			if (!doesNodeExist(path, curator)) {
				String result = curator.create().withMode(CreateMode.EPHEMERAL).forPath(path);
			}
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}
}
