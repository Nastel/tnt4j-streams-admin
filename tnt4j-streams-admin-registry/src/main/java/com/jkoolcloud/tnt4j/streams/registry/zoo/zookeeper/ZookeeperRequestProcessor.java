/*
 * Copyright 2014-2019 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.JsonRpcGeneric;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.IoUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.LoggerWrapper;

/**
 * The type Zookeeper request processor.
 */
public class ZookeeperRequestProcessor {

	private void invokeMethodWrapper(String classReference, Map<String, Object> params) throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Class<?> cls = Class.forName(classReference);
		Method methods = cls.getMethod("processRequest", Object.class);

		Object obj = cls.newInstance();
		methods.invoke(obj, params);
	}

	@SuppressWarnings("unchecked")
	private void processRequest(String method, JsonRpcGeneric jsonRpcRequest, Properties properties) {
		Map<String, Object> params = (Map<String, Object>) jsonRpcRequest.getParams();
		params.put("properties", properties);
		try {
			invokeMethodWrapper(method, params);
		} catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException
				| ClassNotFoundException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	public void methodSelector(JsonRpcGeneric jsonRpcRequest) {
		Properties properties = IoUtils.propertiesWrapper(System.getProperty("listeners"));

		String method = properties.getProperty(jsonRpcRequest.getMethod());

		if (method != null) {
			processRequest(method, jsonRpcRequest, properties);
		} else {
			LoggerWrapper.addMessage(OpLevel.WARNING, "Received incorrect request");
		}
	}

      /*
    private void processReplayBlocks(JsonRpcGeneric jsonRpcRequest){
        Properties properties = IoUtils.propertiesWrapper(System.getProperty("listeners"));
        List<String> params = (List<String>) jsonRpcRequest.getParams();

        Map<String,Object> placeholderToValue = new HashMap<>();
        placeholderToValue.put("blocks", "45645445564,5644565645,78799789,9899");

        String template = null;
        try {
            template = FileUtils.readFile(System.getProperty("replay"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String resolvedString = StringUtils.substitutePlaceholders(template, placeholderToValue);

        File file = new File("./replayBlocks.xml");

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.write(resolvedString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StreamsAgent.runFromAPI(file.getPath());

        file.delete();
    }
*/
}
