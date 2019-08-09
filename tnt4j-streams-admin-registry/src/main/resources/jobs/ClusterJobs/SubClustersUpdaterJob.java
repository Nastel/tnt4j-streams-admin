package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.ClusterJobs;

import java.util.HashMap;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.ThreadUtils;

public class SubClustersUpdaterJob implements Job {
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

		String path = ThreadUtils.getPathToNode(jobDataMap);
		Config config = ThreadUtils.createConfigObject(jobDataMap);

		Map<String, Object> data = new HashMap<>();
		data.put("clusterData", "clusterData");

		ConfigData configData = new ConfigData<>(config, data);

		String response = ThreadUtils.toJson(configData);

		boolean wasSet = CuratorUtils.setData(path, response);

		if (!wasSet) {
			LoggerWrapper.addQuartzJobLog(getClass().getName(), path, response);
		}
	}
}
