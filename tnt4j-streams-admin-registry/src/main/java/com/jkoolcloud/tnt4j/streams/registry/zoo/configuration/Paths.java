
/*
 * Copyright 2014-2020 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
