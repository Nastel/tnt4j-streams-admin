package com.jkoolcloud.tnt4j.streams.registry.zoo.requestExecutors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.Config;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.ConfigData;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.*;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ThreadDump implements JsonRpcRequest<Map<String,Object>> {

    @Override
    public void processRequest(Map<String,Object> params) {

            String path = (String) params.get("responsePath");
            Properties properties = (Properties) params.get("properties");

            String threadDump = RuntimeInformation.getThreadDump();
            String currentTime = TimeUtils.getCurrentTimeStr("America/New_York");

            Config config = new Config();

            config.setComponentLoad(properties.getProperty("componentLoad.threadDump"));
            config.setNodeName(properties.getProperty("nodeName.threadDump"));

            Map<String, Object> stringObjectMap = new HashMap<>();

            stringObjectMap.put("threadDump", threadDump);
            stringObjectMap.put("timestamp", currentTime);

            ConfigData<Map> configData = new ConfigData<>(config, stringObjectMap);

            String threadDumpResponse = null;
            try {
                threadDumpResponse = StaticObjectMapper.mapper.writeValueAsString(configData);
            } catch (JsonProcessingException e) {
                LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
            }

            CuratorUtils.setData(path, threadDumpResponse, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());

    }

}
