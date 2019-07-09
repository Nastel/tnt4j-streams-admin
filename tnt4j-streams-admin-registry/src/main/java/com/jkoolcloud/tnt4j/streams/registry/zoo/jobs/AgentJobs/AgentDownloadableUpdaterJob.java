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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentDownloadableUpdaterJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LoggerWrapper.addMessage(OpLevel.INFO, "Starting AgentDownloadableUpdaterJob");

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        Config config = JobUtils.createConfigObject(jobDataMap);
        String path = JobUtils.getPathToNode(jobDataMap);

        String logsPath = (String) jobDataMap.get("logsPath");

        List<String> logs = IoUtils.getAvailableFiles(logsPath);

        Map<String, Object> downloadablesMap = new HashMap<>();
        downloadablesMap.put("logs", logs);

        ConfigData configData = new ConfigData<>(config, downloadablesMap);

        String response = JobUtils.toJson(configData);

        boolean wasSet = CuratorUtils.setData(path, response, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());

        LoggerWrapper.addMessage(OpLevel.INFO, String.format("Downloadable  was sent: %b", wasSet ));
    }
}
