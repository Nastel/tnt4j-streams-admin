package com.jkoolcloud.tnt4j.streams.registry.zoo.stats;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jkoolcloud.tnt4j.streams.registry.zoo.RestEndpoint.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StaticObjectMapper;

public class ClusterStats {

	private static final MetadataProvider metadataProvider;

	static {
		metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));
	}

	public static String getClusters() {

		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getClustersUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);

		return StaticObjectMapper.objectToString(box);

	}

	public static String getCluster() {
		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getClusterUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);

		return StaticObjectMapper.objectToString(box);
	}

}
