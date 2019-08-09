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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.curator.framework.recipes.queue.SimpleDistributedQueue;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StringUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;
import com.jkoolcloud.tnt4j.uuid.DefaultUUIDFactory;

public class BlockReplay implements JsonRpcRequest<Map<String, Object>> {

	private static AtomicBoolean hasMethodStarted = new AtomicBoolean(false);

	private static SimpleDistributedQueue simpleDistributedQueue;

	private void putToZkQueue(String message) {
		try {
			simpleDistributedQueue.offer(message.getBytes());
		} catch (Exception e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	private String prepareTemplate(Map<String, Object> placeholderToValue, String templatePath) {
		String template = null;
		try {
			template = com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils.readFile(templatePath,
					Charset.defaultCharset());
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

	private int executeConfig(String streamsBat, String streamsRootDir, String pathToConfig)
			throws IOException, InterruptedException {

		// Process process = null;

		ProcessBuilder processBuilder = new ProcessBuilder(streamsBat, "-f:" + pathToConfig);
		processBuilder.directory(new File(streamsRootDir));
		processBuilder.inheritIO();

		Process process = processBuilder.start();

		process.waitFor();

		return process.exitValue();
	}

	@Override
	public void processRequest(Map<String, Object> params) {

		if (!hasMethodStarted.get()) {
			hasMethodStarted.set(true);
		} else {
			putToZkQueue("Max calls reached");
			return;
		}

		String responsePath = (String) params.get("responsePath");
		String streamName = (String) params.get("streamName");
		List<String> blocks = (List) params.get("items");

		simpleDistributedQueue = new SimpleDistributedQueue(
				CuratorSingleton.getSynchronizedCurator().getCuratorFramework(), responsePath);

		Properties properties = FileUtils.getProperties(System.getProperty("listeners"));

		Map<String, Object> streamPlaceholderToValue = new HashMap<>();
		streamPlaceholderToValue.put("blocks", blocks);

		String streamTemplatePath = properties.getProperty(streamName);

		if (streamTemplatePath == null || streamTemplatePath.isEmpty()) {
			putToZkQueue("Replay is not supported for this stream");
			hasMethodStarted.set(false);
			return;
		}

		String logTemplatePath = properties.getProperty("logTemplatePath");

		String userRequests = properties.getProperty("userRequests");
		String streamsBat = properties.getProperty("streamsBat");
		String streamsRootDir = properties.getProperty("streamsRootDir");
		String configsPath = properties.getProperty("configsPath");
		String uuid = DefaultUUIDFactory.getInstance().newUUID();

		Map<String, Object> logPlaceholderToValue = new HashMap<>();
		logPlaceholderToValue.put("userDir", uuid);

		String config = prepareTemplate(streamPlaceholderToValue, streamTemplatePath);
		String log = prepareTemplate(logPlaceholderToValue, logTemplatePath);

		String userDirPath = createDir(userRequests, uuid);

		String savedConfigPath = saveConfig(config, userDirPath, uuid + ".xml");
		// saveConfig(log, configsPath, "log4jRequest.properties");

		int exitValue = -99;
		try {
			putToZkQueue(String.format("Starting replay, streamName: %s blocks: %s", streamName, blocks));
			exitValue = executeConfig(streamsBat, streamsRootDir, savedConfigPath);
		} catch (IOException | InterruptedException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
			// putToZkQueue(String.format("Failed to replay blocks, streamName: %s blocks: %s", streamName, blocks));
		}

		hasMethodStarted.set(false);
	}
}
