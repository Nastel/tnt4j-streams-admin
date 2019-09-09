
package com.jkoolcloud.tnt4j.streams.registry.zoo.configuration;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "path", "config", "data" })
public class StreamNode {

	@JsonProperty("name")
	public String name;
	@JsonProperty("path")
	public String path;
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

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}
}
