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
import com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.*;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Agent config updater job.
 */
public class AgentSamplesConfigUpdaterJob implements Job {

    /**
     * execute.
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LoggerWrapper.addMessage(OpLevel.INFO, "Starting AgentConfigUpdaterJob");

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        Config config = JobUtils.createConfigObject(jobDataMap);
        String path = JobUtils.getPathToNode(jobDataMap);

        String regCommand = RuntimeInformation.getMainConfigPath();

        String mainConfigPath = IoUtils.extractPathFromRegistry(regCommand);


        List<String>  parsersNamesList = null;
        try {
             parsersNamesList = IoUtils.getParsersList(mainConfigPath);
        } catch (Exception e) {
            parsersNamesList = new ArrayList<>();
        }



        String ethStreamConfigsPath = (String) jobDataMap.get("ethStreamConfigsPath");

        List<File> fileList = new ArrayList<>();

        FileUtils.listf(ethStreamConfigsPath, fileList);


        List<String> parserNamesListWithFileExtension = parsersNamesList.stream().map(name -> name + ".xml" ).collect(Collectors.toList());

        parserNamesListWithFileExtension.add(mainConfigPath.substring(mainConfigPath.lastIndexOf("\\")+1));

        List<File> filtered = fileList.stream().filter(file -> file.getName().toLowerCase().endsWith(".xml") && parserNamesListWithFileExtension.contains(file.getName()))
                .collect(Collectors.toList());



        List<Map<String, Object>> mapList = new ArrayList<>();
        for(File file : filtered){
            mapList.add(IoUtils.FileNameAndContentToMap(file, "name", "config"));
        }


        ConfigData configData = new ConfigData<>(config, mapList);

        String response = JobUtils.toJson(configData);


        boolean wasSet = CuratorUtils.setData(path, response, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());

        LoggerWrapper.addMessage(OpLevel.INFO, String.format("Config update was sent: %b", wasSet ));

    }
}
