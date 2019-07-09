/*
 * Copyright 2014-2019 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkoolcloud.tnt4j.streams.registry.zoo.jobs.AgentJobs;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.admin.utils.log.StringBufferAppender;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JobUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Agent logs updater job.
 */
public class AgentLogsUpdaterJob implements Job {


    /**
     * execute.
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        LoggerWrapper.addMessage(OpLevel.INFO, "Starting AgentLogsUpdaterJob");

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        Config config = JobUtils.createConfigObject(jobDataMap);
        String path = JobUtils.getPathToNode(jobDataMap);


        StringBufferAppender stringBufferAppenderNormal = null;
        StringBufferAppender stringBufferAppenderError = null;

        Logger logger = Logger.getRootLogger();

        stringBufferAppenderNormal = (StringBufferAppender) logger.getAppender("myAppender");
        stringBufferAppenderError = (StringBufferAppender) logger.getAppender("myErrorAppender");



        Map<String, Object> data = new HashMap<>();

        if(stringBufferAppenderNormal == null && stringBufferAppenderError == null) {
            data.put("Service log", "");
            data.put("Service error log", "");
        }else {
            data.put("Service log", stringBufferAppenderNormal.getLogs());
            data.put("Service error log", stringBufferAppenderError.getLogs());
        }

        ConfigData configData = new ConfigData<>(config, data);

        String response = JobUtils.toJson(configData);

        boolean wasSet = CuratorUtils.setData(path, response, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
        LoggerWrapper.addMessage(OpLevel.INFO, String.format("Logs update was sent: %b", wasSet));


    }
}
