package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.StreamsJobs;

import java.util.List;
import java.util.Properties;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.IoUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JobUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.ZkTree;

public class StreamRepositoryStatusJob implements Job {
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

		ThreadGroup threadGroup = JobUtils.getThreadGroupByName("com.jkoolcloud.tnt4j.streams.StreamsAgentThreads");

		if (threadGroup == null) {
			return;
		}

		List<StreamThread> streamThreadList = JobUtils.getThreadsByClass(threadGroup, StreamThread.class);

		String agentPath = ZkTree.pathToAgent;

		for (StreamThread streamThread : streamThreadList) {
			String repositoryInfoPath = agentPath + "/" + streamThread.getTarget().getName() + "/" + "repositoryStatus";
			Config config = JobUtils.createConfigObject(jobDataMap);

			Properties properties = IoUtils.getProperties(System.getProperty("zkTree"));

			String link = properties.getProperty(streamThread.getTarget().getName() + ".repository");

			if (link == null) {
				link = "";
			}

			ConfigData configData = new ConfigData<>(config, link);

			String json = JobUtils.toJson(configData);

			boolean wasSet = CuratorUtils.setData(repositoryInfoPath, json);

			if (!wasSet) {
				LoggerWrapper.addQuartzJobLog(getClass().getName(), repositoryInfoPath, json);
			}
		}
	}
}
