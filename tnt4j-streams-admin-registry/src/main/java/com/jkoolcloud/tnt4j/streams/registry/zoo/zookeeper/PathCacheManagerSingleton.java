package com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;

public class PathCacheManagerSingleton {

	private static PathCacheManager pathCacheManager;

	private PathCacheManagerSingleton() {
	}

	public static void Init(CuratorFramework curatorFramework, String nodePath, Boolean cacheData) {
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
