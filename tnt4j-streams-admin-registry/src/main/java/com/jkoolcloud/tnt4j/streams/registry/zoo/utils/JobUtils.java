package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.quartz.JobDataMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;

;

public class JobUtils {

	public static Config createConfigObject(JobDataMap jobDataMap) {

		Config config = new Config();

		String nodeName = (String) jobDataMap.get("nodeName");
		String componentLoad = (String) jobDataMap.get("componentLoad");
		String streamsIcon = (String) jobDataMap.get("streamsIcon");
		String capabilities = (String) jobDataMap.get("capabilities");
		String showBottomLog = (String) jobDataMap.get("showBottomLog");
		String blockchain = (String) jobDataMap.get("blockchain");

		config.setNodeName(nodeName);
		config.setComponentLoad(componentLoad);
		config.setStreamsIcon(streamsIcon);
		config.setCapabilities(capabilities);
		config.setshowBottomLog(showBottomLog);
		config.setBlockchain(blockchain);

		return config;
	}

	public static String getPathToNode(JobDataMap jobDataMap) {
		String path = (String) jobDataMap.get("path");
		return path;
	}

	public static String toJson(ConfigData configData) {
		String response = null;
		try {
			response = StaticObjectMapper.mapper.writeValueAsString(configData);
		} catch (JsonProcessingException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		return response;
	}

	public static String getRegCommand(JobDataMap jobDataMap) {
		String command = (String) jobDataMap.get("regCommand");
		return command;
	}

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
