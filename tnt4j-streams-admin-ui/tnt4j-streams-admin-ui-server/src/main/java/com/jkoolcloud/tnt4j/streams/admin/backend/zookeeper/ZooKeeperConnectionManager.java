package com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.naming.AuthenticationException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;

public class ZooKeeperConnectionManager {
	// public static Map<String, CuratorFramework> connectionMap = new HashMap<>();

	private static LoadingCache<String, CuratorFramework> curatorClientMap;
	private static Boolean initCalled = false;
	private Logger LOG = Logger.getLogger(ZooKeeperConnectionManager.class);
	private String connectionToken;

	public ZooKeeperConnectionManager() {
		if (!initCalled) {
			addCuratorCache();
		}
	}

	public CuratorFramework addClientConnection(String credentialsSent) {
		CuratorFramework tempClient = null;
		try {
			String ZOOKEEPER_URL = PropertyData.getProperty("ZooKeeperAddress");
			CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString(ZOOKEEPER_URL)
					.retryPolicy(new ExponentialBackoffRetry(1000, 3))
					.authorization("digest", credentialsSent.getBytes());
			tempClient = builder.build();
			return tempClient;
		} catch (IOException e) {
			LOG.error("problem on reading property for ZooKeeper connection");
			e.printStackTrace();
		}
		return null;
	}

	public void removeClientConnection() {
		try {
			// LOG.info("REMOVE USER WIHT TOKEN");
			// connectionMap.remove(connectionToken);
			curatorClientMap.invalidate(connectionToken);
		} catch (Exception e) {
			LOG.error("problem on reading property for ZooKeeper connection");
			e.printStackTrace();
		}
	}

	public CuratorFramework getClientConnection() throws AuthenticationException {
		if (checkIfUserExistInCache()) {
			return curatorClientMap.getIfPresent(connectionToken);
		} else {
			throw new AuthenticationException();
		}
	}

	public void setClientConnection(CuratorFramework client) {
		// connectionMap.put(connectionToken, client);
		curatorClientMap.put(connectionToken, client);
		// LOG.info("Connection map "+connectionMap);
	}

	public String getConnectionToken() {
		return connectionToken;
	}

	public void setConnectionToken(String connectionToken) {
		// LOG.info("Set CONNECTION TOKEN "+connectionToken);
		this.connectionToken = connectionToken;
	}

	private void addCuratorCache() {
		try {
			initCalled = true;
			int tokenExpirationTime = Integer.parseInt(PropertyData.getProperty("tokenExpirationTimeGuava"));
			curatorClientMap = CacheBuilder.newBuilder().expireAfterWrite(tokenExpirationTime, TimeUnit.MINUTES)
					.build(new CacheLoader<String, CuratorFramework>() {
						@Override
						public CuratorFramework load(String key) {
							return addClientConnection(key);
						}
					});
		} catch (Exception e) {
			LOG.error("Problem on cache for curator clients");
		}
	}

	/**
	 * Check if a user with the token is inside cache and return the boolean response of it
	 * 
	 * @return
	 */
	public Boolean checkIfUserExistInCache() {
		CuratorFramework curator = curatorClientMap.getIfPresent(connectionToken);
		if (curator != null) {
			// LOG.info("The curator client from cache exist: "+ curator);
			curatorClientMap.put(connectionToken, curator);
			return true;
		} else {
			return false;
		}
	}
}
