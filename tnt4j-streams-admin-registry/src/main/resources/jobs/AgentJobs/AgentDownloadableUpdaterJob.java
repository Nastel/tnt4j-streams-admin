package classBin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jkoolcloud.tnt4j.streams.registry.zoo.configuration.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.FileUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JsonUtils;

public class AgentDownloadableUpdaterJob {

	/*
	 * public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
	 * 
	 * JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap(); Config config =
	 * JobUtils.createConfigObject(jobDataMap); String path = JobUtils.getPathToNode(jobDataMap); String logsPath =
	 * (String) jobDataMap.get("logsPath"); List<String> logs = IoUtils.getAvailableFiles(logsPath);
	 * 
	 * Map<String, Object> downloadablesMap = new HashMap<>(); downloadablesMap.put("logs", logs);
	 * 
	 * ConfigData<Map<String, Object>> configData = new ConfigData<>(config, downloadablesMap); String response =
	 * JobUtils.toJson(configData);
	 * 
	 * boolean wasSet = CuratorUtils.setData(path, response,
	 * CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
	 * 
	 * if (!wasSet) { LoggerWrapper.addQuartzJobLog(this.getClass().getName(), path, response); } }
	 */

	public static String getDownloadables() {

		MetadataProvider metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));

		Map<String, Object> uiMetadata = JsonUtils.jsonToMap(metadataProvider.getDownloadablesUiMetadata(),
				new TypeReference<HashMap>() {
				});
		String logsPath = metadataProvider.getLogsPath();

		List<String> logs = FileUtils.getAvailableFiles(logsPath);

		Map<String, Object> downloadablesMap = new HashMap<>();

		downloadablesMap.put("logs", logs);

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", downloadablesMap);

		String response = null;
		try {
			response = JsonUtils.mapper.writeValueAsString(box);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return response;
	}

}
