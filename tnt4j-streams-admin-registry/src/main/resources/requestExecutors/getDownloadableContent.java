package com.jkoolcloud.tnt4j.streams.registry.zoo.requestExecutors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JsonUtils;

public class getDownloadableContent implements JsonRpcRequest<Map<String, Object>> {

	public final int MAX_FILE_SIZE_BYTES = 1_000_000; // 1mb

	private Boolean doesExceedSizeLimit(String response, int limit) throws UnsupportedEncodingException {

		byte[] bytes = response.getBytes();

		return bytes.length < limit;
	}

	@Override
	public void processRequest(Map<String, Object> params) {

		String responsePath = (String) params.get("responsePath");
		String fileToFetch = (String) params.get("fileName");

		Properties properties = (Properties) params.get("properties");
		String contentPath = properties.getProperty("contentPath");

		String filePath = FileUtils.findFile(contentPath, fileToFetch);
		String content = null;

		try {
			content = com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils.readFile(filePath,
					Charset.defaultCharset());
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		byte[] compressedBytes = null;
		try {
			compressedBytes = FileUtils.compress(content.getBytes(), fileToFetch + ".txt");
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		byte[] base64EncodedBytes = Base64.getEncoder().encode(compressedBytes);

		Map<String, Object> response = new HashMap<>();
		response.put("filename", fileToFetch);
		response.put("data", new String(base64EncodedBytes));

		String responseJson = null;
		try {
			responseJson = JsonUtils.mapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		byte[] bytes = responseJson.getBytes();

		try {
			if (doesExceedSizeLimit(responseJson, MAX_FILE_SIZE_BYTES)) {
				CuratorUtils.setData(responsePath, responseJson);
			} else {
				CuratorUtils.setData(responsePath, "Error: File is to big");
			}
		} catch (UnsupportedEncodingException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

	}
}
