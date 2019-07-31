package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;

public class JobUtils {

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

	public static List<StreamThread> getThreadsByClass(ThreadGroup threadGroup, Class<?> cls) {
		Thread[] threads = new Thread[threadGroup.activeCount()];

		threadGroup.enumerate(threads);

		List<StreamThread> streamThreadList = new ArrayList<>();

		for (Thread thread : threads) {
			if (cls.isInstance(thread)) {
				streamThreadList.add((StreamThread) thread);
			}
		}

		return streamThreadList;
	}

	// Method tries to extract lib version from jar manifest.xml
	public static Map<String, Object> getLibsVersions(String path, List<String> libNames) {
		File[] files = IoUtils.listAllFiles(path);

		Map<String, Object> streamsVersionsMap = IoUtils.getLibVersion(files, libNames);

		return streamsVersionsMap;
	}

}
