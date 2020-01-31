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

package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

public class ThreadUtils {

	public static ThreadGroup getThreadGroupByName(String group) {
		ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();

		while (rootGroup.getParent() != null) {
			rootGroup = rootGroup.getParent();
		}

		ThreadGroup[] threadGroup = new ThreadGroup[rootGroup.activeCount()];

		rootGroup.enumerate(threadGroup, true);

		for (ThreadGroup threadGroup1 : threadGroup) {
			if (threadGroup1 != null && threadGroup1.getName().equals(group)) {
				return threadGroup1;
			}
		}

		return null;
	}
	/*
	 * 
	 * public static List<StreamThread> getThreadsByClass(ThreadGroup threadGroup, Class<?> cls) { Thread[] threads =
	 * new Thread[threadGroup.activeCount()];
	 * 
	 * threadGroup.enumerate(threads);
	 * 
	 * List<StreamThread> streamThreadList = new ArrayList<>();
	 * 
	 * for (Thread thread : threads) { if (cls.isInstance(thread)) { streamThreadList.add((StreamThread) thread); } }
	 * 
	 * return streamThreadList; }
	 */
}
