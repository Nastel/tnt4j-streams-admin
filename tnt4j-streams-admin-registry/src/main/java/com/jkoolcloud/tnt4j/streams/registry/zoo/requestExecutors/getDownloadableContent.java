package com.jkoolcloud.tnt4j.streams.registry.zoo.requestExecutors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.CuratorUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.IoUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StaticObjectMapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper.CuratorSingleton;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class getDownloadableContent implements JsonRpcRequest<Map<String, Object>> {


    public final int MAX_FILE_SIZE_BYTES = 1_000_000; // 1mb


    private Boolean doesExceedSizeLimit(String response, int limit) throws UnsupportedEncodingException {

        byte[] bytes = response.getBytes();

        if(bytes.length < limit) {
            return true;
        }
        return false;
    }


    @Override
    public void processRequest(Map<String, Object> params) {

        String responsePath = (String) params.get("responsePath");
        String fileToFetch = (String) params.get("fileName");

        Properties properties = (Properties) params.get("properties");
        String contentPath = properties.getProperty("contentPath");

        String filePath = IoUtils.findFile(contentPath, fileToFetch);
        String content = null;

        try {
            content = FileUtils.readFile(filePath, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] compressedBytes = null;
        try {
            compressedBytes = IoUtils.compress(content.getBytes(), fileToFetch + ".txt");
        } catch (IOException e) {
            LoggerWrapper.addMessage(OpLevel.ERROR, String.format("could not compress %s", e.getMessage())) ;
        }

        byte[] base64EncodedBytes = Base64.getEncoder().encode(compressedBytes);


        Map<String, Object> response = new HashMap<>();
        response.put("filename", fileToFetch);
        response.put("data", new String(base64EncodedBytes));


        String responseJson = null;
        try {
            responseJson = StaticObjectMapper.mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            LoggerWrapper.addMessage(OpLevel.ERROR, String.format("could not map to json %s", e.getMessage())) ;
        }

        byte[] bytes = responseJson.getBytes();

        content = null;
        base64EncodedBytes = null;
        compressedBytes = null;


        try {
            if(doesExceedSizeLimit(responseJson, MAX_FILE_SIZE_BYTES)) {
                CuratorUtils.setData(responsePath, responseJson, CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
            } else {
                CuratorUtils.setData(responsePath, "Error: File is to big", CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
            }
        } catch (UnsupportedEncodingException e) {
            LoggerWrapper.addMessage(OpLevel.ERROR, e.getMessage()) ;
        }

    }
}
