
package com.jkoolcloud.tnt4j.streams.registry.zoo.configuration;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "logsPath", "sampleCfgsPath", "mainConfigPath", "replayTemplatePath", "userRequestsPath",
		"monitoredFolder" })
public class Paths {

	@JsonProperty("logsPath")
	private String logsPath;
	@JsonProperty("sampleCfgsPath")
	private String sampleCfgsPath;
	@JsonProperty("mainConfigPath")
	private String mainConfigPath;
	@JsonProperty("sampleDirectoryPath")
	private String sampleDirectoryPath;
	@JsonProperty("replayTemplatePath")
	private String replayTemplatePath;
	@JsonProperty("monitoredPath")
	private String monitoredPath;
	@JsonProperty("librariesPath")
	private String librariesPath;
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

	public String getLogsPath() {
		return logsPath;
	}

	public String getSampleCfgsPath() {
		return sampleCfgsPath;
	}

	public String getMainConfigPath() {
		return mainConfigPath;
	}

	public String getSampleDirectoryPath() {
		return sampleDirectoryPath;
	}

	public String getReplayTemplatePath() {
		return replayTemplatePath;
	}

	public String getMonitoredPath() {
		return monitoredPath;
	}

	public String getLibrariesPath() {
		return librariesPath;
	}
}
