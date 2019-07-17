package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.ClusterJobs;

import java.util.HashMap;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JobUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;

public class RootClusterUpdaterJob implements Job {
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

		String path = JobUtils.getPathToNode(jobDataMap);
		Config config = JobUtils.createConfigObject(jobDataMap);

		Map<String, Object> data = new HashMap<>();
		data.put("clusterData", "clusterData");

		ConfigData configData = new ConfigData<>(config, data);

		String response = JobUtils.toJson(configData);

		boolean wasSet = CuratorUtils.setData(path, response,
				CuratorSingleton.getSynchronizedCurator().getCuratorFramework());

		if (!wasSet) {
			LoggerWrapper.addQuartzJobLog(this.getClass().getName(), path, response);
		}

	}
}
