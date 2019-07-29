package com.jkoolcloud.tnt4j.streams.registry.zoo.RestEndpoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;

public class MetadataProvider {

	private Properties properties;

	private void loadProperties(String path) {
		properties = new Properties();
		try (FileInputStream inputFileStream = new FileInputStream(path)) {
			properties.load(inputFileStream);
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	public MetadataProvider(String path) {
		loadProperties(path);
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
}
