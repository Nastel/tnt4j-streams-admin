/*
 * Copyright 2014-2020 JKOOL, LLC.
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

package com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper;

import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathCacheManager {
	private static final Logger LOG = LoggerFactory.getLogger(PathCacheManager.class);

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
