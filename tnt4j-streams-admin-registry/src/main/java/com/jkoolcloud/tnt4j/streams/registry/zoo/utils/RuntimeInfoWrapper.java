package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

import com.jkoolcloud.tnt4j.core.OpLevel;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RuntimeInfoWrapper {


    private static Map<String, Object> getStreamsToClassMapWrapper() {


        Map<String, Object> streamToClassMap = null;
        try {
            streamToClassMap = IoUtils.getStreamsAndClasses(RuntimeInformation.getMainConfigPath());
        } catch (Exception e) {
            LoggerWrapper.addMessage(OpLevel.ERROR, String.format("Failed to extract classes from stream config err msg: %s", e.getMessage()));
        }

        return streamToClassMap;
    }


    public static Map<String, Object> getServiceProperties() {

        Map<String, Object> serviceInfo = new HashMap<>();
        String basePath = RuntimeInformation.getBasePath();


        Map<String, Object> streamToClassMap = getStreamsToClassMapWrapper();

        for(String streamName : streamToClassMap.keySet()){
            String serviceName;
            String fullServiceName;
            String streamType;

            try {
                 serviceName =   RuntimeInformation.getServiceName(streamToClassMap.get(streamName).toString());
            } catch (ClassNotFoundException e) {
                serviceName = "unknownServiceName";
            }

            try {
                 streamType =  RuntimeInformation.getStreamType(streamToClassMap.get(streamName).toString());
            } catch (ClassNotFoundException e) {
                streamType = "unknownStreamType";
            }

            try {
                fullServiceName = RuntimeInformation.getFullName(streamToClassMap.get(streamName).toString());
            } catch (ClassNotFoundException e) {
                fullServiceName = "unknownFullName";
            }

            serviceInfo.put("Name", serviceName);
            serviceInfo.put("Full name", fullServiceName);
            serviceInfo.put("Stream type", streamType);
        }


        serviceInfo.put("Base path", basePath);

        return serviceInfo;
    }

    public static Map<String, Object> getOsProperties() {

        Map<String, Object> osInfo = new HashMap<>();

        String os = RuntimeInformation.getOs();
        String osArch = RuntimeInformation.getOsArch();
        String osVersion = RuntimeInformation.getOsVersion();

        osInfo.put("OS", os);
        osInfo.put("OS architecture", osArch);
        osInfo.put("OS version", osVersion);

        return osInfo;
    }


    public static Map<String, Object> getNetworkProperties() {

        Map<String, Object> networkInfo = new HashMap<>();

        String hostName = null;
        try {
            hostName = RuntimeInformation.getHostName();
        } catch (UnknownHostException e) {
            hostName = "UnknownHost";
        }

        String hostAddress = null;

        try {
            hostAddress = RuntimeInformation.getHostAddress();
        } catch (UnknownHostException e) {
            hostAddress = "UnknownAddress";
        }


        networkInfo.put("Hostname", hostName);
        networkInfo.put("Host address", hostAddress);


        return networkInfo;
    }

    public static Map<String, Object> getCpuProperties() {

        Map<String, Object> cpuInfoMap = new HashMap<>();

        Integer availableCpuCount = RuntimeInformation.getAvailableCpuCount();
        Double systemCpuLoad = RuntimeInformation.getSystemCpuLoad();

        cpuInfoMap.put("Available CPU count", availableCpuCount);
        cpuInfoMap.put("System CPU load [0-1]", systemCpuLoad);

        return cpuInfoMap;
    }

    public static Map<String, Object> getDiscProperties() {
        Map<String, Object> discInfo = new HashMap<>();

        Long totalDiskSpace = RuntimeInformation.getSystemTotalDiskSpace();
        Long freeDiskSpace = RuntimeInformation.getSystemDiskFreeSpace();
        Long diskSpaceUsed = RuntimeInformation.getSystemDiskUsedSpace();

        discInfo.put("Total disk space (bytes)", totalDiskSpace);
        discInfo.put("Free disk space (bytes)", freeDiskSpace);
        discInfo.put("Used disk space (bytes)", diskSpaceUsed);

        return discInfo;
    }

    public static Map<String, Object> getVersionsProperties() {
        Map<String, Object> versions = new HashMap<>();

        Map<String, Object> streamVersionsMap = JobUtils.getLibsVersions(System.getProperty("libraries"), Arrays.asList("tnt4j", "tnt4j-streams-core", "jesl"));

        String javaVersion = RuntimeInformation.getJavaVersion();
        String javaVmVersion = RuntimeInformation.getJavaVmVersion();


        versions.put("TNT4J version", streamVersionsMap.get("tnt4j"));
        versions.put("JESL version", streamVersionsMap.get("jesl"));
        versions.put("Streams Core version", streamVersionsMap.get("tnt4j-streams-core"));
        versions.put("Java version", javaVersion);
        versions.put("Java VM version", javaVmVersion);


        return versions;
    }

    public static Map<String, Object> getConfigsProperties() {
        Map<String, Object> configsInfo = new HashMap<>();


        String log4jConfigPath = RuntimeInformation.getLog4jConfigPath();
        String tnt4jLogPath = RuntimeInformation.getTnt4jLogPath();
        String quartzConfigPath = RuntimeInformation.getQuartzConfigPath();
        String zkTreeConfigPath = RuntimeInformation.getZkTreeConfigPath();

        String mainConfig = RuntimeInformation.getMainConfigPath();
        String samplesPath = RuntimeInformation.getSamplesPath(mainConfig);

        configsInfo.put("Log4j config path", log4jConfigPath);
        configsInfo.put("TNT4J config path", tnt4jLogPath);
        configsInfo.put("Quartz config path", quartzConfigPath);
        configsInfo.put("Zookeeper tree config path", zkTreeConfigPath);
        configsInfo.put("Main stream agent config path", mainConfig);
        configsInfo.put("Stream configs (samples)", samplesPath);

        return configsInfo;
    }

    public static Map<String, Object> getMemoryProperties() {
        Map<String, Object> memoryInfoMap = new HashMap<>();

        Long totalPhysicalMemorySize = RuntimeInformation.getTotalPhysicalMemorySize();
        Long freePhysicalMemorySize = RuntimeInformation.getFreePhysicalMemorySize();
        Long usedPhysicalMemorySize = RuntimeInformation.getUsedPhysicalMemorySize();

        memoryInfoMap.put("Total system RAM (bytes)", totalPhysicalMemorySize);
        memoryInfoMap.put("Free system RAM (bytes)", freePhysicalMemorySize);
        memoryInfoMap.put("Used system RAM (bytes)", usedPhysicalMemorySize);

        return memoryInfoMap;
    }


    public static Map<String, Object> getStreamsAgentCpuLoadProperties() {
        Map<String, Object> streamsAgentCpuLoadMap = new HashMap<>();

        Integer threadCount = RuntimeInformation.getThreadCount();
        Double processCpuLoad = RuntimeInformation.getProcessCpuLoad();
        Integer peakThreadCount = RuntimeInformation.getPeakThreadCount();

        streamsAgentCpuLoadMap.put("Process CPU load [0-1]", processCpuLoad);
        streamsAgentCpuLoadMap.put("Thread count", threadCount);
        streamsAgentCpuLoadMap.put("Peak thread count", peakThreadCount);

        return streamsAgentCpuLoadMap;
    }

    public static Map<String, Object> getStreamsAgentMemoryProperties() {
        Map<String, Object> streamsAgentMemoryInfo = new HashMap<>();

        String heapMemoryUsage = RuntimeInformation.getHeapMemoryUsage();
        String nonHeapMemoryUsage = RuntimeInformation.getNonHeapMemoryUsage();
        Long totalPhysicalMemory = RuntimeInformation.getJvmTotalPhysicalMemory();
        Long freePhysicalMemory = RuntimeInformation.getJvmFreePhysicalMemory();
        Long usedPhysicalMemory = RuntimeInformation.getJvmUsedPhysicalMemory();
        Long maxPhysicalMemory = RuntimeInformation.getJvmMaxPhysicalMemory();

        streamsAgentMemoryInfo.put("Total JVM RAM memory (bytes)", totalPhysicalMemory);
        streamsAgentMemoryInfo.put("Free JVM RAM memory (bytes)", freePhysicalMemory);
        streamsAgentMemoryInfo.put("used JVM RAM memory (bytes)", usedPhysicalMemory);
        streamsAgentMemoryInfo.put("Max JVM RAM memory (bytes)", maxPhysicalMemory);
        streamsAgentMemoryInfo.put("Heap memory usage", heapMemoryUsage);
        streamsAgentMemoryInfo.put("Non heap memory usage", nonHeapMemoryUsage);

        return streamsAgentMemoryInfo;
    }



}
