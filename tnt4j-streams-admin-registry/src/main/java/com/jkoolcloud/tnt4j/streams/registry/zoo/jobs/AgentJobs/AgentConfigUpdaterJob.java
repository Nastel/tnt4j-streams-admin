package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.AgentJobs;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.IoUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JobUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;

public class AgentConfigUpdaterJob implements Job {

	private static final Logger LOG = LoggerFactory.getLogger(AgentConfigUpdaterJob.class);

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		Set<Map<String, Object>> fullConfigurationsList = new HashSet<>();
		JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

		Config config = JobUtils.createConfigObject(jobDataMap);
		String path = JobUtils.getPathToNode(jobDataMap);

		String configsPath = (String) jobDataMap.get("configsPath");

		List<Map<String, Object>> configs = IoUtils.getConfigs(configsPath);
		List<Map<String, Object>> configs2 = IoUtils.getConfigFilesSystemProp();

		Stream.of(configs, configs2).forEach(fullConfigurationsList::addAll);

		ConfigData configData = new ConfigData<>(config, fullConfigurationsList);

		String response = JobUtils.toJson(configData);

		boolean wasSet = CuratorUtils.setData(path, response,
				CuratorSingleton.getSynchronizedCurator().getCuratorFramework());

		if (!wasSet) {
			LoggerWrapper.addQuartzJobLog(getClass().getName(), path, response);
		}
	}
}
