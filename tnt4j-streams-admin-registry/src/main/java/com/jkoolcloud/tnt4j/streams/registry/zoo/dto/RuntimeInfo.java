package com.jkoolcloud.tnt4j.streams.registry.zoo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName("data")
public class RuntimeInfo {


    @JsonProperty("Service")
    private Map<String, Object> service;

    @JsonProperty("Operating system")
    private Map<String, Object> os;

    @JsonProperty("Network")
    private Map<String, Object> network;

    @JsonProperty("CPU")
    private Map<String, Object> cpu;

    @JsonProperty("Memory")
    private Map<String, Object> memory;

    @JsonProperty("StreamAgent CPU usage")
    private Map<String, Object> streamsAgentCpu;

    @JsonProperty("StreamAgent memory")
    private Map<String, Object> streamsAgentMemory;

    @JsonProperty("Disk")
    private Map<String, Object> disk;

    @JsonProperty("Versions")
    private Map<String, Object> versions;

    @JsonProperty("Configs")
    private Map<String, Object> configs;



    public void setOs(Map<String, Object> os) {
        this.os = os;
    }

    public void setNetwork(Map<String, Object> network) {
        this.network = network;
    }

    public void setCpu(Map<String, Object> cpu) {
        this.cpu = cpu;
    }

    public void setMemory(Map<String, Object> memory) {
        this.memory = memory;
    }

    public void setStreamsAgentCpu(Map<String, Object> streamsAgentCpu) {
        this.streamsAgentCpu = streamsAgentCpu;
    }

    public void setStreamsAgentMemory(Map<String, Object> streamsAgentMemory) { this.streamsAgentMemory = streamsAgentMemory; }

    public void setDisk(Map<String, Object> disk) {
        this.disk = disk;
    }

    public void setVersions(Map<String, Object> versions) {
        this.versions = versions;
    }

    public void setConfigs(Map<String, Object> configs) {
        this.configs = configs;
    }

    public void setService(Map<String, Object> service) {
        this.service = service;
    }

}
