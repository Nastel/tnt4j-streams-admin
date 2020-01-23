package com.jkoolcloud.tnt4j.streams.registry.zoo.watcher.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "configurations" })
public class Root {

	@JsonProperty("configurations")
	public List<Configuration> configurations = new ArrayList<>();
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

	public void addConfiguration(Configuration configuration) {
		configurations.add(configuration);
	}

}
