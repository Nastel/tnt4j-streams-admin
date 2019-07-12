package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.StreamsJobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.*;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.ZkTree;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.Properties;

public class StreamIncompleteDataJob implements Job {
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
            String incompleteNodePath = agentPath + "/" + streamThread.getTarget().getName() + "/" + "incomplete";
            if (CuratorUtils.doesNodeExist(incompleteNodePath, CuratorSingleton.getSynchronizedCurator().getCuratorFramework())) {
                Config config = JobUtils.createConfigObject(jobDataMap);


                Properties properties = IoUtils.propertiesWrapper(System.getProperty("zkTree"));


                String link = properties.getProperty(streamThread.getTarget().getName() + ".incomplete");

                if(link == null){
                    link = "";

                }

                ConfigData configData = new ConfigData<>(config, link);

                String json = null;

                try {
                    json = StaticObjectMapper.mapper.writeValueAsString(configData);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }


               boolean wasSet = CuratorUtils.setData(incompleteNodePath, json, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());

                if (!wasSet) {
                    LoggerWrapper.addQuartzJobLog(this.getClass().getName(), incompleteNodePath, json);
                }
            }

        }
    }
}
