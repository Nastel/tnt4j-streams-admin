
package com.jkoolcloud.tnt4j.streams.registry.zoo.configuration;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "port", "securePort", "keyStorePath", "keyStorePassword", "keyManagerPassword" })
public class Jetty {

	@JsonProperty("port")
	private Integer port;
	@JsonProperty("securePort")
	private Integer securePort;
	@JsonProperty("keyStorePath")
	private String keyStorePath;
	@JsonProperty("keyStorePassword")
	private String keyStorePassword;
	@JsonProperty("keyManagerPassword")
	private String keyManagerPassword;
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

	public Integer getPort() {
		return port;
	}

	public Integer getSecurePort() {
		return securePort;
	}

	public String getKeyStorePath() {
		return keyStorePath;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public String getKeyManagerPassword() {
		return keyManagerPassword;
	}
}
