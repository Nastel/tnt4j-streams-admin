
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
