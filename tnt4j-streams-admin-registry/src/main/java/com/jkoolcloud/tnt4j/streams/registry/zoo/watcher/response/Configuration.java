
package com.jkoolcloud.tnt4j.streams.registry.zoo.watcher.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "location", "streams" })
public class Configuration {

	@JsonProperty("id")
	public String id;
	@JsonProperty("streams")
	public List<String> streams = null;
	@JsonProperty("running")
	public boolean running = false;

	public Configuration() {
	}

	public Configuration(String location, List<String> streams) {
		id = location;
		this.streams = streams;
	}

	public Configuration(String location, List<String> streams, boolean running) {
		id = location;
		this.streams = streams;
		this.running = running;
	}

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

}
