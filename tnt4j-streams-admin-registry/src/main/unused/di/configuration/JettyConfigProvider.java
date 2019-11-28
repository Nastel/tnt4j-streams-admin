package com.jkoolcloud.tnt4j.streams.registry.zoo.configuration;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.ValidatorUtils;

public class JettyConfigProvider {

	private PropertiesWrapper properties;

	private static final String ERROR_MESSAGE = "Path >>>%s<<< should point to a file/directory, check config";

	public JettyConfigProvider(String path) {
		boolean isResourceAvailable = ValidatorUtils.isResourceAvailable(path, ValidatorUtils.Resource.FILE);

		if (!isResourceAvailable) {
			LoggerWrapper.addMessage(OpLevel.ERROR, String.format(ERROR_MESSAGE, path));
			throw new IllegalArgumentException(String.format(ERROR_MESSAGE, path));
		}

		properties = new PropertiesWrapper(FileUtils.getProperties(path));
	}

	public String getJettySecurePort() {
		return properties.getProperty("jetty.ssl.securePort");
	}

	public String getKeyStorePath() {
		String keyStorePath = properties.getProperty("jetty.ssl.keyStorePath");

		ValidatorUtils.isResourceAvailable(keyStorePath, ValidatorUtils.Resource.FILE);

		return keyStorePath;
	}

	public String getKeyStorePassword() {
		return properties.getProperty("jetty.ssl.keyStorePassword");
	}

	public String getKeyManagerPassword() {
		return properties.getProperty("jetty.ssl.keyManagerPassword");
	}

}
