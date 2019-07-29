package com.jkoolcloud.tnt4j.streams.registry.zoo.stats;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.StreamsAgent;
import com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StringUtils;

public class StreamControls {

	private static AtomicBoolean hasMethodStarted;

	static {
		hasMethodStarted = new AtomicBoolean();
		hasMethodStarted.set(false);
	}

	public static void restartStreams(String streamName) {

		try {
			StreamsAgent.restartStreams(streamName);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void stopStream(String streamName) {

		try {
			StreamsAgent.stopStreams(streamName);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

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

	public void processRequest(String replayTemplatePath, String logTemplatePath, String userRequestsPath,
			String streamsBatPath, String streamsRootDirPath, Map<String, Object> params) {

		/*
		 * 
		 * if (!hasMethodStarted.get()) { hasMethodStarted.set(true); } else { return; }
		 * 
		 * String responsePath = (String) params.get("responsePath"); String streamName = (String)
		 * params.get("streamName"); List<String> blocks = (List) params.get("items");
		 * 
		 * Properties properties = IoUtils.getProperties(System.getProperty("listeners"));
		 * 
		 * Map<String, Object> streamPlaceholderToValue = new HashMap<>(); streamPlaceholderToValue.put("blocks",
		 * blocks);
		 * 
		 * String streamTemplatePath = properties.getProperty(streamName);
		 * 
		 * if (streamTemplatePath == null || streamTemplatePath.isEmpty()) { hasMethodStarted.set(false); return; }
		 * 
		 * String logTemplatePath = properties.getProperty("logTemplatePath");
		 * 
		 * String userRequests = properties.getProperty("userRequests"); String streamsBat =
		 * properties.getProperty("streamsBat"); String streamsRootDir = properties.getProperty("streamsRootDir");
		 * String configsPath = properties.getProperty("configsPath"); String uuid =
		 * DefaultUUIDFactory.getInstance().newUUID();
		 * 
		 * Map<String, Object> logPlaceholderToValue = new HashMap<>(); logPlaceholderToValue.put("userDir", uuid);
		 * 
		 * String config = prepareTemplate(streamPlaceholderToValue, streamTemplatePath); String log =
		 * prepareTemplate(logPlaceholderToValue, logTemplatePath);
		 * 
		 * String userDirPath = createDir(userRequests, uuid);
		 * 
		 * String savedConfigPath = saveConfig(config, userDirPath, uuid + ".xml");
		 * 
		 * int exitValue = -99; try { exitValue = executeConfig(streamsBat, streamsRootDir, savedConfigPath); } catch
		 * (IOException | InterruptedException e) { LoggerWrapper.logStackTrace(OpLevel.ERROR, e); }
		 * 
		 * hasMethodStarted.set(false);
		 */
	}

}
