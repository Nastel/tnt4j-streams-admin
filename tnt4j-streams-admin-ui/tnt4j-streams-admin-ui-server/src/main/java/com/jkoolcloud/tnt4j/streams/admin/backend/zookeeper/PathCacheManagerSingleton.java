package com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathCacheManagerSingleton {
	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperAccessService.class);
	private static PathCacheManager pathCacheManager;

	private PathCacheManagerSingleton() {
	}

	public static void Init(CuratorFramework curatorFramework, String nodePath, Boolean cacheData) {
		LOG.info("Initializing path cache manager - Singleton");
		if (pathCacheManager == null) {
			pathCacheManager = new PathCacheManager(new PathChildrenCache(curatorFramework, nodePath, cacheData));
		}
	}

	public static PathCacheManager getPathCacheManager() {
		if (pathCacheManager == null) {
			throw new AssertionError("You have to call init first");
		}
		return pathCacheManager;
	}
}
