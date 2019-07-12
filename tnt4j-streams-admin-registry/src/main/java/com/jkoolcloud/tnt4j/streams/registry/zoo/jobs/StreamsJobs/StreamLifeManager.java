package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.StreamsJobs;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.inputs.StreamThread;
import com.jkoolcloud.tnt4j.streams.registry.zoo.misc.StreamManagerSingleton;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JobUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StreamLifeManager implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        ThreadGroup threadGroup = JobUtils.getThreadGroupByName("com.jkoolcloud.tnt4j.streams.StreamsAgentThreads");

        if(threadGroup == null){
            return;
        }

        List<StreamThread> streamThreadList = JobUtils.getThreadsByClass(threadGroup, StreamThread.class);

        List<String> activeStreams = new ArrayList<>();

        for(StreamThread streamThread : streamThreadList){
            activeStreams.add(streamThread.getTarget().getName());
        }

        Set<String> registeredStreams = StreamManagerSingleton.getInstance().getStreamNamesSet();

        registeredStreams.removeAll(activeStreams);

        for(String stream : registeredStreams){
            StreamManagerSingleton.getInstance().closeStream(stream);
        }

    }
}
