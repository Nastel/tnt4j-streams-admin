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

package unused;

import java.util.Map;
import java.util.Set;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * The type Tree cache manager.
 */
public class TreeCacheManager {

	/**
	 * The Path to tree cache.
	 */
	Map<String, TreeCache> pathToTreeCache;

	/**
	 * Instantiates a new Tree cache manager.
	 *
	 * @param pathToTreeCache
	 *            the path to tree cache
	 */
	public TreeCacheManager(Map<String, TreeCache> pathToTreeCache) {
		this.pathToTreeCache = pathToTreeCache;
	}

	private TreeCache createInstance(CuratorFramework client, String path) {
		return TreeCache.newBuilder(client, path).setCacheData(false).build();
	}

	/**
	 * Register tree cache for path.
	 *
	 * @param path
	 *            the path
	 * @param curatorFramework
	 *            the curator framework
	 */
	public void registerTreeCacheForPath(String path, CuratorFramework curatorFramework) {
		TreeCache treeCache = createInstance(curatorFramework, path);
		pathToTreeCache.put(path, treeCache);
	}

	/**
	 * Add listener to path.
	 *
	 * @param path
	 *            the path
	 * @param treeCacheListener
	 *            the tree cache listener
	 */
	public void addListenerToPath(String path, TreeCacheListener treeCacheListener) {
		TreeCache treeCache = pathToTreeCache.get(path);
		treeCache.getListenable().addListener(treeCacheListener);
	}

	/**
	 * Start tree cache listener.
	 *
	 * @param path
	 *            the path
	 */
	public void startTreeCacheListener(String path) {
		TreeCache treeCache = pathToTreeCache.get(path);

		try {
			treeCache.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close tree cache listener.
	 *
	 * @param path
	 *            the path
	 */
	public void closeTreeCacheListener(String path) {
		TreeCache treeCache = pathToTreeCache.get(path);
		treeCache.close();
	}

	/**
	 * Start all listeners.
	 */
	public void startAllListeners() {
		Set<String> paths = pathToTreeCache.keySet();
		for (String path : paths) {
			startTreeCacheListener(path);
		}
	}

	/**
	 * Close all listeners.
	 */
	public void closeAllListeners() {
		Set<String> paths = pathToTreeCache.keySet();
		for (String path : paths) {
			closeTreeCacheListener(path);
		}
	}

}
