package com.jkoolcloud.tnt4j.streams.registry.zoo.stats;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.admin.utils.log.StringBufferAppender;
import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStreamStatistics;
import com.jkoolcloud.tnt4j.streams.registry.zoo.Init;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.RuntimeInfo;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.runtime.RuntimeInfoWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.runtime.RuntimeInformation;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JsonUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.ThreadUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.TimeUtils;

public class AgentStats {

	public static String getConfigs() {
		List<Map<String, Object>> configs = FileUtils.getConfigFilesSystemProp();

		return JsonUtils.objectToString(configs);
	}

	private static String getFile(String file, String path) {
		String filePath = FileUtils.findFile(path, file);
		String content = null;

		try {
			content = FileUtils.readFile(filePath, Charset.defaultCharset());
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		byte[] compressedBytes = null;
		try {
			compressedBytes = FileUtils.compress(content.getBytes(), file + ".txt");
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		byte[] base64EncodedBytes = Base64.getEncoder().encode(compressedBytes);

		Map<String, Object> response = new HashMap<>();
		response.put("filename", file);
		response.put("data", new String(base64EncodedBytes));

		return JsonUtils.objectToString(response);
	}

	public static String getFile(String fileName) {
		return AgentStats.getFile(fileName, Init.getPaths().getLogsPath());
	}

	public static String getDownloadables() {
		List<String> logs = FileUtils.getAvailableFiles(Init.getPaths().getLogsPath());
		Map<String, Object> downloadablesMap = new HashMap<>();
		downloadablesMap.put("logs", logs);

		return JsonUtils.objectToString(downloadablesMap);
	}

	public static String getLogs() {
		Logger logger = Logger.getRootLogger();

		StringBufferAppender stringBufferAppenderNormal = (StringBufferAppender) logger.getAppender("myAppender");
		StringBufferAppender stringBufferAppenderError = (StringBufferAppender) logger.getAppender("myErrorAppender");

		Map<String, Object> logs = new HashMap<>();
		logs.put("Service log", stringBufferAppenderNormal == null ? "" : stringBufferAppenderNormal.getLogs());
		logs.put("Service error log", stringBufferAppenderError == null ? "" : stringBufferAppenderError.getLogs());

		return JsonUtils.objectToString(logs);
	}

	public static String agentRuntime() {
		return JsonUtils.objectToString(AgentStats.getRuntime());
	}

	public static String runtimeInformation() {
		return JsonUtils.objectToString(AgentStats.getRuntime());
	}

	public static String getSamples() {
		String streamConfigsPath = Init.getPaths().getSampleCfgsPath();
		String mainConfigPath = Init.getPaths().getMainConfigPath();

		List<String> parsersUriList = null;
		try {
			parsersUriList = FileUtils.getParsersList(mainConfigPath);
		} catch (Exception e) {
			parsersUriList = new ArrayList<>();
		}

		List<Map<String, Object>> cfgNameToContent = new ArrayList<>();

		for (String parserUri : parsersUriList) {
			String pathToParser = streamConfigsPath + "/" + parserUri;
			File file = new File(pathToParser);

			cfgNameToContent.add(FileUtils.FileNameAndContentToMap(file, "name", "config"));
		}
		cfgNameToContent.add(FileUtils.FileNameAndContentToMap(new File(mainConfigPath), "name", "config"));

		return JsonUtils.objectToString(cfgNameToContent);
	}

	public static String getThreadDump() {
		String currentTime = TimeUtils.getCurrentTimeStr();
		String threadDump = RuntimeInformation.getThreadDump();

		Map<String, Object> data = new HashMap<>();
		data.put("threadDump", threadDump);
		data.put("timestamp", currentTime);

		return JsonUtils.objectToString(data);
	}

	public static String getAllStreamsAndMetricsJson() {
		String mainCfgPath = Init.getPaths().getMainConfigPath();
		Map<String, Object> streamToClassMap = null;

		try {
			streamToClassMap = FileUtils.getStreamsAndClasses(mainCfgPath);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		Map<String, Map<String, Metric>> streamToMetricsMap = AgentStats.getAllStreamsAndMetrics();

		for (String stream : streamToClassMap.keySet()) {
			if (!streamToMetricsMap.containsKey(stream)) {
				streamToMetricsMap.put(stream, new HashMap<>());
			}
		}

		return JsonUtils.objectToString(streamToMetricsMap);
	}

	private static Map<String, Map<String, Metric>> getAllStreamsAndMetrics() {
		ThreadGroup threadGroup = ThreadUtils.getThreadGroupByName("com.jkoolcloud.tnt4j.streams.StreamsAgentThreads");

		if (threadGroup == null) {
			return null;
		}

		List<StreamThread> streamThreadList = ThreadUtils.getThreadsByClass(threadGroup, StreamThread.class);
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
