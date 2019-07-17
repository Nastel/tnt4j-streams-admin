package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.AgentJobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.IoUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JobUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;

public class AgentDownloadableUpdaterJob implements Job {
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
		Config config = JobUtils.createConfigObject(jobDataMap);
		String path = JobUtils.getPathToNode(jobDataMap);
		String logsPath = (String) jobDataMap.get("logsPath");
		List<String> logs = IoUtils.getAvailableFiles(logsPath);

		Map<String, Object> downloadablesMap = new HashMap<>();
		downloadablesMap.put("logs", logs);

		ConfigData<Map<String, Object>> configData = new ConfigData<>(config, downloadablesMap);
		String response = JobUtils.toJson(configData);

		boolean wasSet = CuratorUtils.setData(path, response,
				CuratorSingleton.getSynchronizedCurator().getCuratorFramework());

		if (!wasSet) {
			LoggerWrapper.addQuartzJobLog(this.getClass().getName(), path, response);
		}
	}

}
