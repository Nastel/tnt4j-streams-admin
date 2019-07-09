package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.ClusterJobs;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JobUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.Map;

public class SubClustersUpdaterJob implements Job  {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        LoggerWrapper.addMessage(OpLevel.INFO, "Starting SubClustersUpdaterJob");

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        String path = JobUtils.getPathToNode(jobDataMap);
        Config config =  JobUtils.createConfigObject(jobDataMap);

        Map<String, Object> data = new HashMap<>();
        data.put("clusterData", "clusterData");


        ConfigData configData = new ConfigData<>(config, data);

        String response = JobUtils.toJson(configData);


        boolean wasSet = CuratorUtils.setData(path, response, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());

        LoggerWrapper.addMessage(OpLevel.INFO, String.format("SubClusters were updated: %b", wasSet));
    }
}
