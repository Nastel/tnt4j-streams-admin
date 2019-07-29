package com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper;

import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathCacheManager {
	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperAccessService.class);
	private PathChildrenCache pathChildrenCache;

	public PathCacheManager(PathChildrenCache pathToTreeCache) {
		pathChildrenCache = pathToTreeCache;
	}

	public void addListenerToPath(PathChildrenCacheListener pathChildrenCacheListener) {
		LOG.info("Adding listener to path {}", pathChildrenCacheListener);
		pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
	}

	public void startPathCacheListener() throws Exception {
		LOG.info("Starting path cache manager Listener");
		pathChildrenCache.start();
	}

	public void closeTreeCacheListener() throws Exception {
		pathChildrenCache.close();
	}

	public void clear() throws Exception {
		pathChildrenCache.clearAndRefresh();
	}
}
