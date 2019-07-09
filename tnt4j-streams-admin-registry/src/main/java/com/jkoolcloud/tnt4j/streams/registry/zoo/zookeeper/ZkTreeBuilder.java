package com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper;

import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ZkTreeBuilder {

    Properties properties;

    public ZkTreeBuilder(Properties properties){
        this.properties = properties;
    }

    public String createZkTreeImproved(CuratorFramework curatorFramework){

        List<String> zkTree = new ArrayList<>();

        String[] nodeList =  properties.getProperty("nodeList").split(",");

        for(String node : nodeList){
            String nodePath = properties.getProperty(node);
            if(!CuratorUtils.doesNodeExist(nodePath, curatorFramework)){
                CuratorUtils.createNode(node, curatorFramework);
            }
        }

        return properties.getProperty("stream_agent_name");
    }

}
