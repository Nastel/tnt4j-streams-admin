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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jkoolcloud.tnt4j.streams.registry.zoo.RestEndpoint.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.IoUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StaticObjectMapper;

/**
 * The type Agent config updater job.
 */
public class AgentSamplesConfigUpdaterJob {

	/*
	 * 
	 * @Override public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
	 * 
	 * JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
	 * 
	 * Config config = JobUtils.createConfigObject(jobDataMap); String path = JobUtils.getPathToNode(jobDataMap);
	 * 
	 * String mainConfigPath = System.getProperty("mainCfg");
	 * 
	 * List<String> parsersUriList = null; try { parsersUriList = IoUtils.getParsersList(mainConfigPath); } catch
	 * (Exception e) { parsersUriList = new ArrayList<>(); }
	 * 
	 * String streamConfigsPath = (String) jobDataMap.get("streamConfigsPath");
	 * 
	 * List<Map<String, Object>> mapList = new ArrayList<>();
	 * 
	 * for (String parserUri : parsersUriList) { String pathToParser = streamConfigsPath + "/" + parserUri;
	 * 
	 * File file = new File(pathToParser);
	 * 
	 * mapList.add(IoUtils.FileNameAndContentToMap(file, "name", "config")); }
	 * mapList.add(IoUtils.FileNameAndContentToMap(new File(mainConfigPath), "name", "config"));
	 * 
	 * ConfigData configData = new ConfigData<>(config, mapList);
	 * 
	 * String response = JobUtils.toJson(configData);
	 * 
	 * boolean wasSet = CuratorUtils.setData(path, response,
	 * CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
	 * 
	 * if (!wasSet) { LoggerWrapper.addQuartzJobLog(this.getClass().getName(), path, response); } }
	 */

	public static String getSamples() {
		MetadataProvider metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));

		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getSampleConfigsUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		String streamConfigsPath = metadataProvider.getStreamAdminCfgsPath();

		String mainConfigPath = metadataProvider.getMainCfgPath();

		List<String> parsersUriList = null;
		try {
			parsersUriList = IoUtils.getParsersList(mainConfigPath);
		} catch (Exception e) {
			parsersUriList = new ArrayList<>();
		}

		List<Map<String, Object>> mapList = new ArrayList<>();

		for (String parserUri : parsersUriList) {
			String pathToParser = streamConfigsPath + "/" + parserUri;

			File file = new File(pathToParser);

			mapList.add(IoUtils.FileNameAndContentToMap(file, "name", "config"));
		}
		mapList.add(IoUtils.FileNameAndContentToMap(new File(mainConfigPath), "name", "config"));

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", mapList);

		return StaticObjectMapper.objectToString(box);

	}
}
