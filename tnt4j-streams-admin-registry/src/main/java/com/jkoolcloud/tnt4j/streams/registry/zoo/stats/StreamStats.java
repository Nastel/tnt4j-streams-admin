package com.jkoolcloud.tnt4j.streams.registry.zoo.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStreamStatistics;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JsonUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.ThreadUtils;

public class StreamStats {

	private static Map<String, Metric> getMetrics(String streamName) {

		ThreadGroup threadGroup = ThreadUtils.getThreadGroupByName("com.jkoolcloud.tnt4j.streams.StreamsAgentThreads");

		if (threadGroup == null) {
			return null;
		}

		List<StreamThread> streamThreadList = ThreadUtils.getThreadsByClass(threadGroup, StreamThread.class);

		for (StreamThread streamThread : streamThreadList) {

			if (streamThread.getTarget().getName().equals(streamName)) {

				MetricRegistry streamStatistics = TNTInputStreamStatistics.getMetrics(streamThread.getTarget());
				Map<String, Metric> metricRegistry = streamStatistics.getMetrics();

				return metricRegistry;

			}
		}
		return null;
	}

	// TODO merge getMetricsForNode and getMetricsForStreamNode

	public static String getMetricsForNode(String streamName) {

		Map<String, Metric> metricRegistry = StreamStats.getMetrics(streamName);

		return JsonUtils.objectToString(metricRegistry);
	}

	public static String getMetricsForStreamNode(String streamName) {

		Map<String, Metric> metricRegistry = StreamStats.getMetrics(streamName);

		Map<String, Object> box = new HashMap<>();

		return JsonUtils.objectToString(metricRegistry);
	}

}
