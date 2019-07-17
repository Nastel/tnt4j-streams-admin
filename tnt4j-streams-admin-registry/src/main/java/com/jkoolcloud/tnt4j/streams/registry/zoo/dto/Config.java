package com.jkoolcloud.tnt4j.streams.registry.zoo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "nodeName",
        "componentLoad",
        "logNavigation",
        "streamsIcon",
        "capabilities",
        "showBottomLog"
})
public class Config {

	@JsonProperty("nodeName")
	private String nodeName;
	@JsonProperty("componentLoad")
	private String componentLoad;
	@JsonProperty("streamsIcon")
	private String streamsIcon;
	@JsonProperty("showBottomLog")
	private String showBottomLog;
	@JsonProperty("capabilities")
	private String capabilities;
	@JsonProperty("logNavigation")
	private List<String> logNavigation;

	@JsonProperty("logNavigation")
	public List<String> getLogNavigation() {
		return logNavigation;
	}

	@JsonProperty("logNavigation")
	public void setLogNavigation(List<String> logNavigation) {
		this.logNavigation = logNavigation;
	}

	@JsonProperty("nodeName")
	public String getNodeName() {
		return nodeName;
	}

	@JsonProperty("nodeName")
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	@JsonProperty("componentLoad")
	public String getComponentLoad() {
		return componentLoad;
	}

	@JsonProperty("componentLoad")
	public void setComponentLoad(String componentLoad) {
		this.componentLoad = componentLoad;
	}

	@JsonProperty("streamsIcon")
	public String getStreamsIcon() {
		return streamsIcon;
	}

	@JsonProperty("streamsIcon")
	public void setStreamsIcon(String streamsIcon) {
		this.streamsIcon = streamsIcon;
	}

	@JsonProperty("showBottomLog")
	public String getshowBottomLog() {
		return showBottomLog;
	}

	@JsonProperty("showBottomLog")
	public void setshowBottomLog(String showBottomLog) {
		this.showBottomLog = showBottomLog;
	}

	@JsonProperty("capabilities")
	public String getCapabilities() {
		return capabilities;
	}

	@JsonProperty("capabilities")
	public void setCapabilities(String capabilities) {
		this.capabilities = capabilities;
	}

}
