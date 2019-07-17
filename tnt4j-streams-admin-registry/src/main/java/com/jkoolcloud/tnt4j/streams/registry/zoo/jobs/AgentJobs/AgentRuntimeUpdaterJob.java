package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.AgentJobs;

import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.RuntimeInfo;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JobUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.RuntimeInfoWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;

public class AgentRuntimeUpdaterJob implements Job {

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

		String path = JobUtils.getPathToNode(jobDataMap);
		Config config = JobUtils.createConfigObject(jobDataMap);

		Map<String, Object> osMap = RuntimeInfoWrapper.getOsProperties();
		Map<String, Object> network = RuntimeInfoWrapper.getNetworkProperties();
		Map<String, Object> cpu = RuntimeInfoWrapper.getCpuProperties();
		Map<String, Object> memory = RuntimeInfoWrapper.getMemoryProperties();
		Map<String, Object> streamsAgentCpuLoad = RuntimeInfoWrapper.getStreamsAgentCpuLoadProperties();
		Map<String, Object> streamsAgentMemory = RuntimeInfoWrapper.getStreamsAgentMemoryProperties();
		Map<String, Object> disc = RuntimeInfoWrapper.getDiscProperties();
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

		ConfigData configData = new ConfigData<>(config, runtimeInfo);

		String response = JobUtils.toJson(configData);

		boolean wasSet = CuratorUtils.setData(path, response,
				CuratorSingleton.getSynchronizedCurator().getCuratorFramework());

		if (!wasSet) {
			LoggerWrapper.addQuartzJobLog(this.getClass().getName(), path, response);
		}
	}
}
