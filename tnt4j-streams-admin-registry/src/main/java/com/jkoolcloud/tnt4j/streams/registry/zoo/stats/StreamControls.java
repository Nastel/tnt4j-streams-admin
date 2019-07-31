package com.jkoolcloud.tnt4j.streams.registry.zoo.stats;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.StreamsAgent;
import com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.RestEndpoint.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StringUtils;
import com.jkoolcloud.tnt4j.uuid.DefaultUUIDFactory;

public class StreamControls {

	private static MetadataProvider metadataProvider;

	static {
		metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));
	}

	public static void restartStreams(String streamName) {
		StreamsAgent.restartStreams(streamName);
	}

	public static void stopStream(String streamName) {
		StreamsAgent.stopStreams(streamName);
	}

	private static String prepareTemplate(Map<String, Object> placeholderToValue, String templatePath) {
		String template = null;
		try {
			template = FileUtils.readFile(templatePath, Charset.defaultCharset());
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		return StringUtils.substitutePlaceholders(template, placeholderToValue);
	}

	private static String saveConfig(String config, String dirPath, String cfgName) {
		Path pathToConfig = null;
		try {
			pathToConfig = Files.write(Paths.get(dirPath + "/" + cfgName), config.getBytes(), CREATE, WRITE);
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		return pathToConfig.toString();
	}

	public static void processRequest(String[] blocksArr) {
		String streamTemplatePath = metadataProvider.getReplayTemplatePath();

		Map<String, Object> streamPlaceholderToValue = new HashMap<>();
		streamPlaceholderToValue.put("blocks", Arrays.asList(blocksArr));

		if (streamTemplatePath == null || streamTemplatePath.isEmpty()) {
			return;
		}

		String userRequests = metadataProvider.getMonitoredFolder();

		String uuid = DefaultUUIDFactory.getInstance().newUUID();

		String config = prepareTemplate(streamPlaceholderToValue, streamTemplatePath);

		saveConfig(config, userRequests, uuid + ".xml");
	}

}
