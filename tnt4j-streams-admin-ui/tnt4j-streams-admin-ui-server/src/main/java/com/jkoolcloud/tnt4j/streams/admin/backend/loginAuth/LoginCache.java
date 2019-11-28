package com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.naming.AuthenticationException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jkoolcloud.tnt4j.streams.admin.backend.reCaptcha.CaptchaUtils;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;
import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.ZooKeeperConnectionManager;
import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.ZookeeperAccessService;

public class LoginCache {
	private static Logger LOG = Logger.getLogger(LoginCache.class);
	private static LoadingCache<String, String> cache;
	private static Boolean initCalled = false;
	private static String loginStatus = "Not checked";
	private static Boolean isUserAdmin;
	private static HashMap userMap = new HashMap<String, HashMap>();
	private static int userCount = 0;
	private static boolean bypassSecurity;
	/** debug */
	private static String token = "";
	ZooKeeperConnectionManager zooManager = new ZooKeeperConnectionManager();

	/**
	 * MEthod used to ensure that only one instance of guava cache would be created
	 */
	public LoginCache() {
		// LOG.info("initialization: {}", initCalled);
		if (!initCalled) {
			init();
		}
	}

	/**
	 * Used for development purposes
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ZookeeperAccessService zooAccess = new ZookeeperAccessService();
		ZooKeeperConnectionManager zooManager = new ZooKeeperConnectionManager();
		zooAccess.init();
		LoginCache login = new LoginCache();
		String userToken = login.generateTokenForUser();
		token = userToken;
		zooManager.setConnectionToken(userToken);
		login.checkIfUserExistInCache(userToken);
		login.checkForSuccessfulLogin("admin:admin");
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/downloadables", 0,
		// userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/threadDump", 0,
		// userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/sampleConfigurations",
		// 0, userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/runtimeInformation",
		// 0, userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/logs", 0,
		// userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/configurations", 0,
		// userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream/incomplete",
		// 0, userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream/metrics",
		// 0, userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream/repository",
		// 0, userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream",
		// 0, userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth", 0, userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/_streamsAndMetrics",
		// 0, userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets", 0, userToken);
		zooAccess.getServiceNodeInfoFromLink(
				"/clusters/clusterBlockchainMainnets/streamsAgentEth/downloadables/streamsAdmin_error.log", 0,
				userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters/clusterBlockchainMainnets/streamsAgentEth/EthereumInfuraStream/_stop",
		// 0, userToken);
		// zooAccess.getServiceNodeInfoFromLink("/clusters", 0, userToken);

		// ObjectMapper objMapper = new ObjectMapper();
		// ObjectWriter writer = objMapper.writer();
		// UsersUtils users = new UsersUtils();
		// HashMap<String, List> clustersWithRights =
		// objMapper.readValue("{\"/streams/v2/clusters/clusterBlockchainMainnets\" : [\"read\",\"admin\",\"action\"],"
		// +
		// "\"/streams/v2/clusters/clusterBlockchainTestnet\" : [\"read\",\"admin\",\"action\"]}", new
		// TypeReference<Map<String, Object>>() {});
		// users.addUser(clustersWithRights,"admin","admin", false, true);
		/**
		 * FOR UPDATING SIMPLE USER TEST HashMap<String, List> clustersWithRights =
		 * objMapper.readValue("{\"/streams/v1/clusters/clusterBlockchainMainnets\" : [\"read\"]," +
		 * "\"/streams/v1/clusters/clusterBlockchainTestnet\" : [\"read\"]}", new TypeReference<Map<String, Object>>()
		 * {}); users.updateUser(clustersWithRights,"basic", "U2FsdGVkX183M6qOtbz6+GvAs9P+shsMiQjxODSWpEQ=", false);
		 */
		/**
		 * FOR UPDATING USER LIST FOR SIMPLE USER List clustersList = Arrays.asList(new
		 * String[]{"/streams/v1/clusters/clusterBlockchainMainnets", "/streams/v1/clusters/clusterBlockchainTestnet"});
		 * users.getUserList(clustersList);
		 */
		CaptchaUtils.verify("sometoken");
		// LOG.info("token: {}", token);
		// LOG.info("The cache set after first initialization: {} - {}", cache.asMap(), cache.getIfPresent(token));
	}

	public static int getUserCount() {
		return userCount;
	}

	/**
	 * Setter for users count set used for response diff and logging
	 * 
	 * @param count
	 */
	public void setUserCount(int count) {
		userCount = count;
	}

	public void newLogin() {
		isUserAdmin = false;
		token = "";
		bypassSecurity = false;
	}

	public Boolean getIsUserAdmin() {
		return isUserAdmin;
	}

	public void setIsUserAdmin(Boolean isUserAdmin) {
		LoginCache.isUserAdmin = isUserAdmin;
	}

	public HashMap getUserMap() {
		return userMap;
	}

	public void setUserMap(int userCount, HashMap userData) {
		userMap.put(userCount, userData);
	}

	public void clearUserMap() {
		userMap.clear();
	}

	/**
	 * Getter for login status set used for response diff and logging
	 * 
	 * @return
	 */
	public String getLoginStatus() {
		return loginStatus;
	}

	/**
	 * Setter for login status set used for response diff and logging
	 * 
	 * @param status
	 */
	public void setLoginStatus(String status) {
		LOG.info(status);
		loginStatus = status;
	}

	/**
	 * If the user does not exist in cache set it as a new user
	 * 
	 * @param value
	 * @return
	 */
	public String setUser(String value) {
		if (!checkIfUserExistInCache(value)) {
			value = "true";
		} else {
			value = "false";
		}
		// LOG.info("Key {}",value);
		return value;
	}

	/**
	 * Check if a user with the token is inside cache and return the boolean response of it
	 * 
	 * @param data
	 * @return
	 */
	public Boolean checkIfUserExistInCache(String data) {
		data = data.replace("\"", "");
		String value = cache.getIfPresent(data);
		if (value != null) {
			return true;
		} else {
			bypassSecurity = false;
			return false;
		}
	}

	/**
	 * Remove the value of the token from guava cache
	 * 
	 * @param token
	 */
	public void removeTheTokenFromCache(String token) {
		bypassSecurity = false;
		cache.invalidate(token);
	}

	/**
	 * Generate and return a new token
	 * 
	 * @return
	 */
	public String generateTokenForUser() {
		// String token = UUID.randomUUID().toString();
		token = UUID.randomUUID().toString();
		LOG.info("Login token: " + token);
		try {
			cache.get(token);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return token;
	}

	/**
	 * Return the token that was generated for the user
	 * 
	 * @return
	 */
	public String getGeneratedToken() {
		return token;
	}

	/**
	 * Getter to log in without security check
	 * 
	 * @return
	 */
	public boolean getBypassSecurity() {
		return bypassSecurity;
	}

	/**
	 * Setter to log in without security check.
	 * 
	 * @param choice
	 */
	public void setBypassSecurity(boolean choice) {
		bypassSecurity = choice;
	}

	/**
	 * Start the cache
	 */
	private void init() {
		try {
			initCalled = true;
			LOG.info("The tokenCacheMap started");
			int tokenExpirationTime = Integer.parseInt(PropertyData.getProperty("tokenExpirationTimeGuava"));
			CacheLoader<String, String> usersCache = new CacheLoader<String, String>() {
				@Override
				public String load(String empId) {
					return setUser(empId);
				}
			};
			cache = CacheBuilder.newBuilder().expireAfterAccess(tokenExpirationTime, TimeUnit.MINUTES)
					.build(usersCache);
		} catch (IOException e) {
			LOG.error("Problem on reading properties file information");
			e.printStackTrace();
		} catch (Exception e) {
			LOG.error("Failed to initialize cache");
			e.printStackTrace();
		}
		// LOG.info("The cache set after first initialization: {} - {}", cache.asMap(), cache.getIfPresent(token));
	}

	public void disconnectFromZooKeeper(String token) {
		try {
			ZookeeperAccessService zooAccess = new ZookeeperAccessService();
			zooManager.setConnectionToken(token);
			CuratorFramework connection = zooManager.getClientConnection();
			zooManager.removeClientConnection();
			ZookeeperAccessService.stopConnectionCurator(connection);
		} catch (AuthenticationException e) {
			LOG.info("Connection timeout: " + e);
		}
	}

	public boolean checkForSuccessfulLogin(String credentials) {
		ZookeeperAccessService zooAccess = new ZookeeperAccessService();
		zooAccess.connectToZooKeeper(credentials);
		return zooAccess.checkIfConnected();
	}
}
