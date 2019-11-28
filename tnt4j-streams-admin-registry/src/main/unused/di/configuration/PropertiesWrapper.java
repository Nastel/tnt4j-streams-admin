package com.jkoolcloud.tnt4j.streams.registry.zoo.configuration;

import java.util.Properties;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;

public class PropertiesWrapper {

	private Properties properties;

	private static final String ERROR_MESSAGE = "Property >>>%s<<< cannot be null or empty, check config";

	public PropertiesWrapper(Properties properties) {
		this.properties = properties;
	}

	public String getProperty(String name) {
		String property = properties.getProperty(name);

		if (property == null || property.isEmpty()) {
			LoggerWrapper.addMessage(OpLevel.ERROR, String.format(ERROR_MESSAGE, name));
			throw new IllegalArgumentException(String.format(ERROR_MESSAGE, name));
		}
		return property;
	}

}
