package classBin;

import java.util.*;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jkoolcloud.tnt4j.streams.registry.zoo.RestEndpoint.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.IoUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StaticObjectMapper;

public class AgentConfigUpdaterJob {

	private static final Logger LOG = LoggerFactory.getLogger(AgentConfigUpdaterJob.class);
	/*
	 * @Override public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
	 * Set<Map<String, Object>> fullConfigurationsList = new HashSet<>(); JobDataMap jobDataMap =
	 * jobExecutionContext.getMergedJobDataMap();
	 * 
	 * Config config = JobUtils.createConfigObject(jobDataMap); String path = JobUtils.getPathToNode(jobDataMap);
	 * 
	 * String configsPath = (String) jobDataMap.get("configsPath");
	 * 
	 * List<Map<String, Object>> configs = IoUtils.getConfigs(configsPath); List<Map<String, Object>> configs2 =
	 * IoUtils.getConfigFilesSystemProp();
	 * 
	 * Stream.of(configs, configs2).forEach(fullConfigurationsList::addAll);
	 * 
	 * ConfigData configData = new ConfigData<>(config, fullConfigurationsList);
	 * 
	 * String response = JobUtils.toJson(configData);
	 * 
	 * boolean wasSet = CuratorUtils.setData(path, response,
	 * CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
	 * 
	 * if (!wasSet) { LoggerWrapper.addQuartzJobLog(getClass().getName(), path, response); } }
	 */

	public static String getConfigs() {
		Set<Map<String, Object>> fullConfigurationsList = new HashSet<>();

		MetadataProvider metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));

		Map<String, Object> uiMetadata = StaticObjectMapper.jsonToMap(metadataProvider.getConfigUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		String configsPath = metadataProvider.getStreamAdminCfgsPath();

		List<Map<String, Object>> configs = IoUtils.getConfigs(configsPath);
		List<Map<String, Object>> configs2 = IoUtils.getConfigFilesSystemProp();

		Stream.of(configs, configs2).forEach(fullConfigurationsList::addAll);

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", fullConfigurationsList);

		return StaticObjectMapper.objectToString(box);
	}
}
