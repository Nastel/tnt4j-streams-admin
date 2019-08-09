package com.jkoolcloud.tnt4j.streams.registry.zoo.configuration;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.ValidatorUtils;

public class MetadataProvider {

	private PropertiesWrapper properties;

	private static final String ERROR_MESSAGE = "Path >>>%s<<< should point to a file/directory, check config";

	public MetadataProvider(String path) {
		boolean isResourceAvailable = ValidatorUtils.isResourceAvailable(path, ValidatorUtils.Resource.FILE);

		if (!isResourceAvailable) {
			LoggerWrapper.addMessage(OpLevel.ERROR, String.format(ERROR_MESSAGE, path));
			throw new IllegalArgumentException(String.format(ERROR_MESSAGE, path));
		}

		properties = new PropertiesWrapper(FileUtils.getProperties(path));
	}

	public String getConfigUiMetadata() {
		return properties.getProperty("configurationsMetadata");
	}

	public String getRuntimeUiMetadata() {
		return properties.getProperty("runtimeInformationMetadata");
	}

	public String getStreamAgentUiMetadata() {
		return properties.getProperty("streamsAgentMetadata");
	}

	public String getLogsUiMetadata() {
		return properties.getProperty("logsMetadata");
	}

	public String getClustersUiMetadata() {
		return properties.getProperty("genericClustersMetadata");
	}

	public String getClusterUiMetadata() {
		return properties.getProperty("namedClusterMetadata");
	}

	public String getDownloadablesUiMetadata() {
		return properties.getProperty("downloadablesMetadata");
	}

	public String getSampleConfigsUiMetadata() {
		return properties.getProperty("sampleConfigurationsMetadata");
	}

	public String getThreadDumpUiMetadata() {
		return properties.getProperty("threadDumpMetadata");
	}

	public String getMetricsUiMetadata() {
		return properties.getProperty("metricsMetadata");
	}

	public String getLogsPath() {
		return properties.getProperty("logsPath");
	}

	public String getStreamAdminCfgsPath() {
		return properties.getProperty("streamAdminCfgPath");
	}

	public String getMainCfgPath() {
		return properties.getProperty("mainConfigPath");
	}

	public String getStreamUiMetadata() {
		return properties.getProperty("streamMetadata");
	}

	public String getSampleCfgsPath() {
		return properties.getProperty("sampleCfgsPath");
	}

	public String getSteamsAgentPath() {
		return properties.getProperty("streamsAgent");
	}

	public String getIncompleteFullData(String streamName) {
		return properties.getProperty(streamName + ".incompleteFullData");
	}

	public String getRepositoryFullData(String streamName) {
		return properties.getProperty(streamName + ".repositoryFullData");
	}

	public String getReplayTemplatePath() {
		return properties.getProperty("replayTemplatePath");
	}

	public String getMonitoredFolder() {
		return properties.getProperty("userRequestsPath");
	}
}
