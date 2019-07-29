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

package classBin;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jkoolcloud.tnt4j.streams.admin.utils.log.StringBufferAppender;
import com.jkoolcloud.tnt4j.streams.registry.zoo.RestEndpoint.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StaticObjectMapper;

/**
 * The type Agent logs updater job.
 */
public class AgentLogsUpdaterJob {

	/*
	 * @Override public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
	 * 
	 * JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
	 * 
	 * Config config = JobUtils.createConfigObject(jobDataMap); String path = JobUtils.getPathToNode(jobDataMap);
	 * 
	 * StringBufferAppender stringBufferAppenderNormal = null; StringBufferAppender stringBufferAppenderError = null;
	 * 
	 * Logger logger = Logger.getRootLogger();
	 * 
	 * stringBufferAppenderNormal = (StringBufferAppender) logger.getAppender("myAppender"); stringBufferAppenderError =
	 * (StringBufferAppender) logger.getAppender("myErrorAppender");
	 * 
	 * Map<String, Object> data = new HashMap<>();
	 * 
	 * if (stringBufferAppenderNormal == null && stringBufferAppenderError == null) { data.put("Service log", "");
	 * data.put("Service error log", ""); } else { data.put("Service log", stringBufferAppenderNormal.getLogs());
	 * data.put("Service error log", stringBufferAppenderError.getLogs()); }
	 * 
	 * ConfigData configData = new ConfigData<>(config, data);
	 * 
	 * String response = JobUtils.toJson(configData);
	 * 
	 * boolean wasSet = CuratorUtils.setData(path, response,
	 * CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
	 * 
	 * if (!wasSet) { LoggerWrapper.addQuartzJobLog(getClass().getName(), path, response); }
	 * 
	 * }
	 */

	public static String getLogs() {
		MetadataProvider metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));

		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getLogsUiMetadata(),
				new TypeReference<HashMap>() {
				});

		StringBufferAppender stringBufferAppenderNormal = null;
		StringBufferAppender stringBufferAppenderError = null;

		Logger logger = Logger.getRootLogger();

		stringBufferAppenderNormal = (StringBufferAppender) logger.getAppender("myAppender");
		stringBufferAppenderError = (StringBufferAppender) logger.getAppender("myErrorAppender");

		Map<String, Object> logs = new HashMap<>();

		if (stringBufferAppenderNormal == null && stringBufferAppenderError == null) {
			logs.put("Service log", "");
			logs.put("Service error log", "");
		} else {
			logs.put("Service log", stringBufferAppenderNormal.getLogs());
			logs.put("Service error log", stringBufferAppenderError.getLogs());
		}

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", logs);

		return StaticObjectMapper.objectToString(box);

	}
}
