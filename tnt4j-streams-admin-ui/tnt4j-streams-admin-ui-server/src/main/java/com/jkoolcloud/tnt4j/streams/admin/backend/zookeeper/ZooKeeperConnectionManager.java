package com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.naming.AuthenticationException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;

public class ZooKeeperConnectionManager {
	// public static Map<String, CuratorFramework> connectionMap = new HashMap<>();

	private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperConnectionManager.class);
	private static LoadingCache<String, CuratorFramework> curatorClientMap;
	private static Boolean initCalled = false;
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
			 //LOG.info("REMOVE USER WITH TOKEN");
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
		LOG.info("Add connection to map "+connectionToken+" "+client);
		curatorClientMap.put(connectionToken, client);
		// LOG.info("Connection map "+connectionMap);
	}


	public void removeClientConnectionAfterTimeout(String token, CuratorFramework connection) {
//		LOG.fatal("Remove ZooKeeper curator connection 2: "+ token);
//		CuratorFramework connection = curatorClientMap.getIfPresent(token);
//		LOG.fatal("Remove ZooKeeper curator connection 3: "+ connection);
		ZookeeperAccessService.stopConnectionCurator(connection);
	}

	public String getConnectionToken() {
		return connectionToken;
	}

	public void setConnectionToken(String connectionToken) {
//		 LOG.info("Set CONNECTION TOKEN "+connectionToken);
		this.connectionToken = connectionToken;
	}

	private void addCuratorCache() {
		try {
			initCalled = true;

			RemovalListener<String, CuratorFramework> removalListener = removal -> {
				LOG.trace("Remove ZooKeeper curator connection: "+ removal.getKey()+ " " + removal.getValue());
				removeClientConnectionAfterTimeout(removal.getKey(), removal.getValue());
			};

			int tokenExpirationTime = Integer.parseInt(PropertyData.getProperty("tokenExpirationTimeGuava"));
			curatorClientMap = CacheBuilder.newBuilder()
					.expireAfterWrite(tokenExpirationTime, TimeUnit.MINUTES)
					.maximumSize(10)
					.removalListener(removalListener)
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
	 * Check if a user with the token is inside cache and return the boolean response of it.
	 * No curratorClientMap.put because the user ession gets destroyed on login on rewrite
	 * 
	 * @return
	 */
	public Boolean checkIfUserExistInCache() {
		CuratorFramework curator = curatorClientMap.getIfPresent(connectionToken);
		if (curator != null) {
//			LOG.info("The curator client from cache exist: "+ connectionToken);
//			curatorClientMap.put(connectionToken, curator);
			return true;
		} else {
			return false;
		}
	}
}
