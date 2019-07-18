package com.jkoolcloud.tnt4j.streams.registry.zoo.requestExecutors;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.curator.framework.recipes.queue.SimpleDistributedQueue;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.IoUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StringUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;
import com.jkoolcloud.tnt4j.uuid.DefaultUUIDFactory;

public class BlockReplay implements JsonRpcRequest<Map<String, Object>> {

	private static SimpleDistributedQueue simpleDistributedQueue;

	private String prepareTemplate(Map<String, Object> placeholderToValue, String templatePath) {
		String template = null;
		try {
			template = FileUtils.readFile(templatePath, Charset.defaultCharset());
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		return StringUtils.substitutePlaceholders(template, placeholderToValue);
	}

	private String createDir(String path, String name) {
		File file = new File(path + "/" + name);

		try {
			file.mkdir();
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		return file.getAbsolutePath();
	}

	private String saveConfig(String config, String dirPath, String cfgName) {
		Path pathToConfig = null;
		try {
			pathToConfig = Files.write(Paths.get(dirPath + "/" + cfgName), config.getBytes());
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		return pathToConfig.toString();
	}

	private int executeConfig(String streamsBat, String streamsRootDir, String pathToConfig) throws IOException {

		Process process = null;
		ProcessBuilder processBuilder = new ProcessBuilder(streamsBat, "-f:" + pathToConfig);
		processBuilder.directory(new File(streamsRootDir));

		processBuilder.inheritIO();

		try {
			process = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return process.exitValue();
	}

	@Override
	public void processRequest(Map<String, Object> params) {

		String responsePath = (String) params.get("responsePath");
		String streamName = (String) params.get("streamName");
		List<String> blocks = (List) params.get("items");

		simpleDistributedQueue = new SimpleDistributedQueue(
				CuratorSingleton.getSynchronizedCurator().getCuratorFramework(), responsePath);

		Properties properties = IoUtils.propertiesWrapper(System.getProperty("listeners"));

		Map<String, Object> streamPlaceholderToValue = new HashMap<>();
		streamPlaceholderToValue.put("blocks", blocks);

		String streamTemplatePath = properties.getProperty(streamName);
		String logTemplatePath = properties.getProperty("logTemplatePath");

		String userRequests = properties.getProperty("userRequests");
		String streamsBat = properties.getProperty("streamsBat");
		String streamsRootDir = properties.getProperty("streamsRootDir");
		String configsPath = properties.getProperty("configsPath");
		String uuid = DefaultUUIDFactory.getInstance().newUUID();

		Map<String, Object> logPlacehorderToValue = new HashMap<>();
		logPlacehorderToValue.put("userDir", uuid);

		String config = prepareTemplate(streamPlaceholderToValue, streamTemplatePath);
		String log = prepareTemplate(logPlacehorderToValue, logTemplatePath);

		String userDirPath = createDir(userRequests, uuid);

		String savedConfigPath = saveConfig(config, userDirPath, uuid + ".xml");
		saveConfig(log, configsPath, "log4jRequest.properties");

		try {
			simpleDistributedQueue.offer("Process has been started".getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}

		int exitValue = -99;
		try {
			exitValue = executeConfig(streamsBat, streamsRootDir, savedConfigPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
