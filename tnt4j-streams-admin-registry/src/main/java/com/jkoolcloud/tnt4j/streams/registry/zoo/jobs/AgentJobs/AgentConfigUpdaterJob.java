package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.AgentJobs;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.IoUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JobUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.Map;

public class AgentConfigUpdaterJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LoggerWrapper.addMessage(OpLevel.INFO, "Starting AgentConfigUpdaterJob");

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        Config config = JobUtils.createConfigObject(jobDataMap);
        String path = JobUtils.getPathToNode(jobDataMap);

        String configsPath = (String) jobDataMap.get("configsPath");

        List<Map<String, Object>> configs = IoUtils.getConfigs(configsPath);

        ConfigData configData = new ConfigData<>(config, configs);

        String response = JobUtils.toJson(configData);

        try {
            boolean wasSet = CuratorUtils.setData(path, response, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
        }catch (Exception e){
            e.printStackTrace();
        }
       // LoggerWrapper.addMessage(OpLevel.INFO, String.format("Config update was sent: %b", wasSet ));
    }
}
