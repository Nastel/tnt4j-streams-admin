package com.jkoolcloud.tnt4j.streams.registry.zoo.stats;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jkoolcloud.tnt4j.streams.registry.zoo.configuration.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JsonUtils;

public class ClusterStats {

	private static final MetadataProvider metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));

	public static String getClusters() {

		Map<String, Object> uiMetadata = JsonUtils.jsonToMap(metadataProvider.getClustersUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);

		return JsonUtils.objectToString(box);

	}

	public static String getCluster() {
		Map<String, Object> uiMetadata = JsonUtils.jsonToMap(metadataProvider.getClusterUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);

		return JsonUtils.objectToString(box);
	}

}
