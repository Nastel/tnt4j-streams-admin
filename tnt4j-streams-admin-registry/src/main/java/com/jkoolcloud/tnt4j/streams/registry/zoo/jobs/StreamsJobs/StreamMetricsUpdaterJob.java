package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.StreamsJobs;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStreamStatistics;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JobUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.ZkTree;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.Map;

public class StreamMetricsUpdaterJob implements Job {


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        LoggerWrapper.addMessage(OpLevel.INFO, "Starting StreamMetricsUpdaterJob");

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        ThreadGroup threadGroup = JobUtils.getThreadGroupByName("com.jkoolcloud.tnt4j.streams.StreamsAgentThreads");

        if (threadGroup == null) {
            return;
        }

        List<StreamThread> streamThreadList = JobUtils.getThreadsByClass(threadGroup, StreamThread.class);

        String agentPath = ZkTree.pathToAgent;


        for (StreamThread streamThread : streamThreadList) {
            String metricsPath = agentPath + "/" + streamThread.getTarget().getName() + "/" + "metrics";
            if (CuratorUtils.doesNodeExist(metricsPath, CuratorSingleton.getSynchronizedCurator().getCuratorFramework())) {


                MetricRegistry streamStatistics = TNTInputStreamStatistics.getMetrics(streamThread.getTarget());


                Map<String, Metric> metricRegistry = streamStatistics.getMetrics();

                Config config = JobUtils.createConfigObject(jobDataMap);

                ConfigData configData = new ConfigData<>(config, metricRegistry);

                String json = JobUtils.toJson(configData);

                boolean wasSet = CuratorUtils.setData(metricsPath, json, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());

                LoggerWrapper.addMessage(OpLevel.INFO, String.format("Metricss update was sent: %b", wasSet));
            }
        }
    }
}
