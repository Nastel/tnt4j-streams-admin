package com.jkoolcloud.tnt4j.streams.registry.zoo.stats;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils;
import com.jkoolcloud.tnt4j.streams.admin.utils.log.StringBufferAppender;
import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStreamStatistics;
import com.jkoolcloud.tnt4j.streams.registry.zoo.RestEndpoint.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.RuntimeInfo;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.*;

public class AgentStats {

	private static final MetadataProvider metadataProvider;

	static {
		metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));
	}

	public static String getConfigs() {

		Set<Map<String, Object>> fullConfigurationsList = new HashSet<>();

		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getConfigUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		String configsPath = metadataProvider.getMainCfgPath();

		List<Map<String, Object>> configs = IoUtils.getConfigs(configsPath);
		List<Map<String, Object>> configs2 = IoUtils.getConfigFilesSystemProp();

		Stream.of(configs, configs2).forEach(fullConfigurationsList::addAll);

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", fullConfigurationsList);

		return StaticObjectMapper.objectToString(box);
	}

	private static String getFile(String file, String path) {

		String contentPath = path;

		String filePath = IoUtils.findFile(contentPath, file);
		String content = null;

		try {
			content = FileUtils.readFile(filePath, Charset.defaultCharset());
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		byte[] compressedBytes = null;
		try {
			compressedBytes = IoUtils.compress(content.getBytes(), file + ".txt");
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		byte[] base64EncodedBytes = Base64.getEncoder().encode(compressedBytes);

		Map<String, Object> response = new HashMap<>();
		response.put("filename", file);
		response.put("data", new String(base64EncodedBytes));

		return StaticObjectMapper.objectToString(response);

	}

	public static String getFile(String fileName) {
		return AgentStats.getFile(fileName, metadataProvider.getLogsPath());
	}

	public static String getDownloadables() {

		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getDownloadablesUiMetadata(),
				new TypeReference<HashMap>() {
				});
		String logsPath = metadataProvider.getLogsPath();

		List<String> logs = IoUtils.getAvailableFiles(logsPath);

		Map<String, Object> downloadablesMap = new HashMap<>();

		downloadablesMap.put("logs", logs);

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", downloadablesMap);

		String response = null;
		try {
			response = StaticObjectMapper.mapper.writeValueAsString(box);
		} catch (JsonProcessingException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
		return response;
	}

	public static String getLogs() {

		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getLogsUiMetadata(),
				new TypeReference<HashMap>() {
				});

		StringBufferAppender stringBufferAppenderNormal = null;
		StringBufferAppender stringBufferAppenderError = null;

		Logger logger = Logger.getRootLogger();

		stringBufferAppenderNormal = (StringBufferAppender) logger.getAppender("myAppender");
		stringBufferAppenderError = (StringBufferAppender) logger.getAppender("myErrorAppender");

		Map<String, Object> logs = new HashMap<>();

		if (stringBufferAppenderNormal == null && stringBufferAppenderError == null) {
			logs.put("Service log", "");
			logs.put("Service error log", "");
		} else {
			logs.put("Service log", stringBufferAppenderNormal.getLogs());
			logs.put("Service error log", stringBufferAppenderError.getLogs());
		}

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", logs);

		return StaticObjectMapper.objectToString(box);
	}

	public static String agentRuntime() {

		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getStreamAgentUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", AgentStats.getRuntime());

		return StaticObjectMapper.objectToString(box);
	}

	public static String runtimeInformation() {

		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getRuntimeUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", AgentStats.getRuntime());

		return StaticObjectMapper.objectToString(box);
	}

	public static String getSamples() {
		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getSampleConfigsUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		String streamConfigsPath = metadataProvider.getSampleCfgsPath();

		String mainConfigPath = metadataProvider.getMainCfgPath();

		List<String> parsersUriList = null;
		try {
			parsersUriList = IoUtils.getParsersList(mainConfigPath);
		} catch (Exception e) {
			parsersUriList = new ArrayList<>();
		}

		List<Map<String, Object>> mapList = new ArrayList<>();

		for (String parserUri : parsersUriList) {
			String pathToParser = streamConfigsPath + "/" + parserUri;

			File file = new File(pathToParser);

			mapList.add(IoUtils.FileNameAndContentToMap(file, "name", "config"));
		}
		mapList.add(IoUtils.FileNameAndContentToMap(new File(mainConfigPath), "name", "config"));

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", mapList);

		return StaticObjectMapper.objectToString(box);
	}

	public static String getThreadDump() {

		String currentTime = TimeUtils.getCurrentTimeStr();

		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getThreadDumpUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		String threadDump = RuntimeInformation.getThreadDump();

		Map<String, Object> data = new HashMap<>();

		data.put("threadDump", threadDump);
		data.put("timestamp", currentTime);

		Map<String, Object> box = new HashMap<>();
		box.put("config", uiMetadata);
		box.put("data", data);

		return StaticObjectMapper.objectToString(box);
	}

	public static String getAllStreamsAndMetricsJson() {

		String mainCfgPath = metadataProvider.getMainCfgPath();

		Map<String, Object> streamToClassMap = null;

		try {
			streamToClassMap = IoUtils.getStreamsAndClasses(mainCfgPath);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		Set<String> streamNames = streamToClassMap.keySet();

		Map<String, Map<String, Metric>> streamToMetricsMap = AgentStats.getAllStreamsAndMetrics();

		for (String stream : streamNames) {
			if (!streamToMetricsMap.containsKey(stream)) {
				streamToMetricsMap.put(stream, new HashMap<>());
			}
		}

		return StaticObjectMapper.objectToString(streamToMetricsMap);
	}

	private static Map<String, Map<String, Metric>> getAllStreamsAndMetrics() {

		ThreadGroup threadGroup = JobUtils.getThreadGroupByName("com.jkoolcloud.tnt4j.streams.StreamsAgentThreads");

		if (threadGroup == null) {
			return null;
		}

		List<StreamThread> streamThreadList = JobUtils.getThreadsByClass(threadGroup, StreamThread.class);
		Map<String, Map<String, Metric>> streamToMetricsMap = new HashMap<>();

		for (StreamThread streamThread : streamThreadList) {
			MetricRegistry streamStatistics = TNTInputStreamStatistics.getMetrics(streamThread.getTarget());
			streamToMetricsMap.put(streamThread.getTarget().getName(), streamStatistics.getMetrics());
		}
		return streamToMetricsMap;
	}

	private static RuntimeInfo getRuntime() {

		Map<String, Object> osMap = RuntimeInfoWrapper.getOsProperties();
		Map<String, Object> network = RuntimeInfoWrapper.getNetworkProperties();
		Map<String, Object> cpu = RuntimeInfoWrapper.getCpuProperties();
		Map<String, Object> memory = RuntimeInfoWrapper.getMemoryProperties();
		Map<String, Object> streamsAgentCpuLoad = RuntimeInfoWrapper.getStreamsAgentCpuLoadProperties();
		Map<String, Object> streamsAgentMemory = RuntimeInfoWrapper.getStreamsAgentMemoryProperties();
		Map<String, Object> disc = RuntimeInfoWrapper.getDiskProperties();
		Map<String, Object> versions = RuntimeInfoWrapper.getVersionsProperties();
		Map<String, Object> configs = RuntimeInfoWrapper.getConfigsProperties();
		Map<String, Object> service = RuntimeInfoWrapper.getServiceProperties();

		RuntimeInfo runtimeInfo = new RuntimeInfo();

		runtimeInfo.setOs(osMap);
		runtimeInfo.setNetwork(network);
		runtimeInfo.setCpu(cpu);
		runtimeInfo.setMemory(memory);
		runtimeInfo.setStreamsAgentCpu(streamsAgentCpuLoad);
		runtimeInfo.setStreamsAgentMemory(streamsAgentMemory);
		runtimeInfo.setDisk(disc);
		runtimeInfo.setVersions(versions);
		runtimeInfo.setConfigs(configs);
		runtimeInfo.setService(service);

		return runtimeInfo;
	}

}
