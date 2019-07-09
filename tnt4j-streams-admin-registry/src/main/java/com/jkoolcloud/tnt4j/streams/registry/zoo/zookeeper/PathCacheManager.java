package com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper;

import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

public class PathCacheManager {


    private PathChildrenCache pathChildrenCache;

    public PathCacheManager(PathChildrenCache pathToTreeCache) {
        this.pathChildrenCache = pathToTreeCache;
    }


    public void addListenerToPath(PathChildrenCacheListener pathChildrenCacheListener) {
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
    }


    public void startPathCacheListener() throws Exception {
        pathChildrenCache.start();
    }


    public void closeTreeCacheListener() throws Exception {
        pathChildrenCache.close();
    }

    public void clear() throws Exception {
        pathChildrenCache.clearAndRefresh();
    }

}
