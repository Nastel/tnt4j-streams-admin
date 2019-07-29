package com.jkoolcloud.tnt4j.streams.registry.zoo.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStreamStatistics;
import com.jkoolcloud.tnt4j.streams.registry.zoo.RestEndpoint.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JobUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StaticObjectMapper;

public class StreamStats {

	private static final MetadataProvider metadataProvider;

	static {
		metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));
	}

	public static String getRepositoryStatus(String streamName) {
		return metadataProvider.getRepositoryFullData(streamName);
	}

	public static String getIncomplete(String streamName) {
		return metadataProvider.getIncompleteFullData(streamName);
	}

	private static Map<String, Metric> getMetrics(String streamName) {

		ThreadGroup threadGroup = JobUtils.getThreadGroupByName("com.jkoolcloud.tnt4j.streams.StreamsAgentThreads");

		if (threadGroup == null) {
			return null;
		}

		List<StreamThread> streamThreadList = JobUtils.getThreadsByClass(threadGroup, StreamThread.class);

		for (StreamThread streamThread : streamThreadList) {

			if (streamThread.getTarget().getName().equals(streamName)) {

				MetricRegistry streamStatistics = TNTInputStreamStatistics.getMetrics(streamThread.getTarget());
				Map<String, Metric> metricRegistry = streamStatistics.getMetrics();

				return metricRegistry;

			}
		}
		return null;
	}

	public static String getMetricsForNode(String streamName) {
		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getMetricsUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		Map<String, Metric> metricRegistry = StreamStats.getMetrics(streamName);

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", metricRegistry);

		return StaticObjectMapper.objectToString(box);
	}

	public static String getMetricsForStreamNode(String streamName) {
		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getStreamUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		Map<String, Metric> metricRegistry = StreamStats.getMetrics(streamName);

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", metricRegistry);

		return StaticObjectMapper.objectToString(box);
	}

}
