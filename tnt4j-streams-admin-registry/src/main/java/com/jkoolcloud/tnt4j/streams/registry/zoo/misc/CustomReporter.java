/*
 * Copyright 2014-2019 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkoolcloud.tnt4j.streams.registry.zoo.misc;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.format.DefaultFormatter;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.utils.LoggerUtils;
import org.apache.curator.framework.CuratorFramework;

/**
 * The type Custom reporter.
 */
public class CustomReporter implements Reporter {

	private static final EventSink LOGGER_ZOOKEEPER = LoggerUtils.getLoggerSink("zookeeperLog"); // NON-NLS

	/**
	 * For registry custom reporter . builder.
	 *
	 * @param registry
	 *            the registry
	 * @return the custom reporter . builder
	 */
	public static CustomReporter.Builder forRegistry(MetricRegistry registry) {
		LOGGER_ZOOKEEPER.setEventFormatter(new DefaultFormatter("{2}")); // NON-NLS
		return new CustomReporter.Builder(registry);
	}

	/**
	 * A builder for {@link com.codahale.metrics.ConsoleReporter} instances. Defaults to using the default locale and
	 * time zone, writing to {@code System.out}, converting rates to events/second, converting durations to
	 * milliseconds, and not filtering metrics.
	 */
	public static class Builder {
		private final MetricRegistry registry;

		private Builder(MetricRegistry registry) {
			this.registry = registry;
		}

		/**
		 * Builds a {@link com.codahale.metrics.ConsoleReporter} with the given properties.
		 *
		 * @return a {@link com.codahale.metrics.ConsoleReporter}
		 */
		public CustomReporter build() {
			return new CustomReporter(registry);
		}
	}

	private MetricRegistry registry;

	private CustomReporter(MetricRegistry registry) {
		this.registry = registry;
	}

	public String report() {
		ObjectMapper objectMapper = new ObjectMapper();
		String gaugesStr = null;
		String countersStr = null;
		String histogramsStr = null;
		String metersStr = null;
		String timersStr = null;

		try {
			gaugesStr = objectMapper.writeValueAsString(registry.getGauges()); // TODO: pick only values of interest
			countersStr = objectMapper.writeValueAsString(registry.getCounters()); // TODO: pick only values of interest
			histogramsStr = objectMapper.writeValueAsString(registry.getHistograms()); // TODO: pick only values of
																						// interest
			metersStr = objectMapper.writeValueAsString(registry.getMeters()); // TODO: pick only values of interest
			timersStr = objectMapper.writeValueAsString(registry.getTimers()); // TODO: pick only values of interest
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append("{") // NON-NLS
				.append("\"gauges\":").append(gaugesStr).append(",") // NON-NLS
				.append("\"counters\":").append(countersStr).append(",") // NON-NLS
				.append("\"histograms\":").append(histogramsStr).append(",") // NON-NLS
				.append("\"meters\":").append(metersStr).append(",") // NON-NLS
				.append("\"timers\":").append(timersStr).append("}"); // NON-NLS

		return messageBuilder.toString();
	}

	/**
	 * Flush directly to zookeeper.
	 *
	 * @param responseDir
	 *            the response dir
	 * @param curatorFramework
	 *            the curator framework
	 */
	public void flushDirectlyToZookeeper(String responseDir, CuratorFramework curatorFramework) {
		String metricsStr = report();

		LOGGER_ZOOKEEPER.log(OpLevel.INFO, String.format("Created message for ZK: %s", metricsStr));

		boolean wasDataSet = CuratorUtils.setData(responseDir, metricsStr, curatorFramework);

		LOGGER_ZOOKEEPER.log(OpLevel.INFO, String.format("Message sent to ZK successfully: %b", wasDataSet));
	}
}
