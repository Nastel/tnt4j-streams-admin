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

import org.quartz.JobExecutionException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jkoolcloud.tnt4j.streams.registry.zoo.RestEndpoint.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.RuntimeInformation;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StaticObjectMapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.TimeUtils;

/**
 * The type Agent thread dump updater job.
 */
public class AgentThreadDumpUpdaterJob {

	/**
	 * execute.
	 *
	 * @throws JobExecutionException
	 * 
	 * @Override public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
	 * 
	 *           JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap(); String path =
	 *           JobUtils.getPathToNode(jobDataMap); Config config = JobUtils.createConfigObject(jobDataMap);
	 * 
	 *           String agentThreadDump = RuntimeInformation.getThreadDump();
	 * 
	 *           Map<String, Object> data = new HashMap<>(); data.put("threadDump", agentThreadDump);
	 * 
	 *           ConfigData configData = new ConfigData<>(config, data);
	 * 
	 *           String response = JobUtils.toJson(configData);
	 * 
	 *           boolean wasSet = CuratorUtils.setData(path, response,
	 *           CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
	 * 
	 *           if (!wasSet) { LoggerWrapper.addQuartzJobLog(this.getClass().getName(), path, response); }
	 * 
	 *           }
	 */

	public static String getThreadDump() {

		MetadataProvider metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));

		String currentTime = TimeUtils.getCurrentTimeStr();

		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getThreadDumpUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		String threadDump = RuntimeInformation.getThreadDump();

		Map<String, Object> data = new HashMap<>();

		data.put("threadDump", threadDump);
		data.put("timestamp", currentTime);

		Map<String, Object> box = new HashMap<>();
		box.put("config", uiMetadata);
		box.put("data", data);

		return StaticObjectMapper.objectToString(box);
	}
}
