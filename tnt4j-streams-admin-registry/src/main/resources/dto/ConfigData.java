package dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "config", "data" })
public class ConfigData<T> {

	@JsonProperty("config")
	Config config;

	@JsonProperty("data")
	private T data;

	public ConfigData() {
	}

	public ConfigData(Config config, T data) {
		this.config = config;
		this.data = data;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public void setData(T data) {
		this.data = data;
	}
}
