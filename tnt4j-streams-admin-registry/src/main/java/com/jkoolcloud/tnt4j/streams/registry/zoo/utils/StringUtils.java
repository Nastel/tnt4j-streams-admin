package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

public class StringUtils {

	public static String substitutePlaceholders(String template, Map<String, Object> placeholderToValue) {
		StringSubstitutor stringSubstitutor = new StringSubstitutor(placeholderToValue);
		String resolved = stringSubstitutor.replace(template);
		return resolved;
	}

}
