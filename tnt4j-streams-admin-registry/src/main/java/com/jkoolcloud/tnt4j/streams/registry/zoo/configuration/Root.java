
package com.jkoolcloud.tnt4j.streams.registry.zoo.configuration;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "jetty", "zookeeper", "paths" })
public class Root {

	@JsonProperty("jetty")
	private Jetty jetty;
	@JsonProperty("zookeeper")
	private Zookeeper zookeeper;
	@JsonProperty("paths")
	private Paths paths;
	@JsonProperty("variables")
	private Variables variables;
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

	public Jetty getJetty() {
		return jetty;
	}

	public Zookeeper getZookeeper() {
		return zookeeper;
	}

	public Paths getPaths() {
		return paths;
	}

	public Variables getVariables() {
		return variables;
	}
}
