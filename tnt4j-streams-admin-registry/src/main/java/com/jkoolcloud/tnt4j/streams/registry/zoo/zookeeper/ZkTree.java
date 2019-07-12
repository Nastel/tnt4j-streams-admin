package com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper;

import com.jkoolcloud.tnt4j.streams.registry.zoo.misc.StreamManagerSingleton;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ZkTree {

    private static Properties properties;

    public static String pathToAgent;


    public static void setProperties(Properties properties0){
        properties = properties0;
    }


    public static String createZkTree(CuratorFramework curatorFramework){

        List<String> zkTree = new ArrayList<>();

        String[] nodeList =  properties.getProperty("nodeList").split(",");

        for(String node : nodeList){
            String nodePath = properties.getProperty(node);
            if(!CuratorUtils.doesNodeExist(nodePath, curatorFramework)){
                CuratorUtils.createNode(node, curatorFramework);
            }
        }

        pathToAgent = properties.getProperty("stream_agent_name");

        return properties.getProperty("stream_agent_name");
    }



    public static void publishStreamServices(String streamName, String path){

        String[] availableStats = properties.getProperty(streamName).split(",");

        String streams = properties.getProperty(streamName);
        if(streams.contains(",")){
            availableStats = properties.getProperty(streamName).split(",");
        }else {
            availableStats = new String[1];
            availableStats[0] = streams;
        }

        for (String stat : availableStats) {
            CuratorUtils.createEphemeralNode(path + "/" + stat, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
        }


        StreamManagerSingleton.getInstance().putStream(streamName, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
    }





    public static void registerStreams(String pathToAgent, CuratorFramework curatorFramework){

        String streams = properties.getProperty("streamList");
        String[] streamsArray;
        if(streams.contains(",")){
             streamsArray = properties.getProperty("streamList").split(",");
        }else {
            streamsArray = new String[1];
            streamsArray[0] = streams;
        }

        for(String stream : streamsArray){
            String path = pathToAgent + "/" + stream;

            if(!CuratorUtils.doesNodeExist(path,curatorFramework)){
                CuratorUtils.createNode(path, curatorFramework);
            }

            publishStreamServices(stream, path);
        }

    }

}
