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

    //Returns path where to put streams
    public static String createZkTree(CuratorFramework curatorFramework){

        List<String> zkTree = new ArrayList<>();

        String path =  properties.getProperty("root");

        if(!CuratorUtils.doesNodeExist(path, curatorFramework)){
            CuratorUtils.createNode(path, curatorFramework);
        }

        zkTree.add ( properties.getProperty("version"));
        zkTree.add ( properties.getProperty("clusters"));
        zkTree.add ( properties.getProperty("cluster_name"));
        zkTree.add ( properties.getProperty("stream_agent_name"));
        zkTree.add ( properties.getProperty("agent_logs"));
        zkTree.add ( properties.getProperty("agent_configurations"));
        zkTree.add ( properties.getProperty("agent_sampleConfigurations"));
        zkTree.add ( properties.getProperty("agent_threadDump"));
        zkTree.add ( properties.getProperty("downloadables"));



        for (String node : zkTree){
            if(!CuratorUtils.doesNodeExist(node, curatorFramework)){
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

        CuratorFramework streamConnection = CuratorFrameworkFactory.newClient("127.0.0.1", new ExponentialBackoffRetry(1000, 1));
        streamConnection.start();

        for (String stat : availableStats) {
            CuratorUtils.createEphemeralNode(path + "/" + stat, streamConnection);
        }


        StreamManagerSingleton.getInstance().putStream(streamName, streamConnection);
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
