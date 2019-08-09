package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.StreamsJobs;

import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStreamStatistics;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.ThreadUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.ZkTree;

public class StreamMetricsUpdaterJob implements Job {

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

		ThreadGroup threadGroup = ThreadUtils.getThreadGroupByName("com.jkoolcloud.tnt4j.streams.StreamsAgentThreads");

		if (threadGroup == null) {
			return;
		}

		List<StreamThread> streamThreadList = ThreadUtils.getThreadsByClass(threadGroup, StreamThread.class);

		String agentPath = ZkTree.pathToAgent;

		for (StreamThread streamThread : streamThreadList) {
			String metricsPath = agentPath + "/" + streamThread.getTarget().getName() + "/" + "metrics";
			MetricRegistry streamStatistics = TNTInputStreamStatistics.getMetrics(streamThread.getTarget());
			Map<String, Metric> metricRegistry = streamStatistics.getMetrics();
			Config config = ThreadUtils.createConfigObject(jobDataMap);
			ConfigData<Map<String, Metric>> configData = new ConfigData<>(config, metricRegistry);
			String json = ThreadUtils.toJson(configData);

			boolean wasSet = CuratorUtils.setData(metricsPath, json);

			if (!wasSet) {
				LoggerWrapper.addQuartzJobLog(getClass().getName(), metricsPath, json);
			}
		}
	}
}
