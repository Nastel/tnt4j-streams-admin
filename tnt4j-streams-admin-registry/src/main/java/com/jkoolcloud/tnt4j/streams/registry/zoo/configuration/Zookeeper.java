
package com.jkoolcloud.tnt4j.streams.registry.zoo.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "connectString", "zklogin", "zkpass", "agentNodes", "streamNodes" })
public class Zookeeper {

	@JsonProperty("connectString")
	private String connectString;
	@JsonProperty("zklogin")
	private String zklogin;
	@JsonProperty("zkpass")
	private String zkpass;
	@JsonProperty("agentNodes")
	private List<AgentNode> agentNodes = null;
	@JsonProperty("streamNodes")
	private List<StreamNode> streamNodes = null;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		additionalProperties.put(name, value);
	}

	public String getConnectString() {
		return connectString;
	}

	public String getZklogin() {
		return zklogin;
	}

	public String getZkpass() {
		return zkpass;
	}

	public List<AgentNode> getAgentNodes() {
		return agentNodes;
	}

	public List<StreamNode> getStreamNodes() {
		return streamNodes;
	}
}
