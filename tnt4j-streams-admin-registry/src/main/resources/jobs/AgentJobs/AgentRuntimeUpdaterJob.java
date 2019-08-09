package classBin;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jkoolcloud.tnt4j.streams.registry.zoo.configuration.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.dto.RuntimeInfo;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.JsonUtils;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.RuntimeInfoWrapper;

public class AgentRuntimeUpdaterJob {

	/*
	 * @Override public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
	 * 
	 * JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
	 * 
	 * String path = JobUtils.getPathToNode(jobDataMap); Config config = JobUtils.createConfigObject(jobDataMap);
	 * 
	 * Map<String, Object> osMap = RuntimeInfoWrapper.getOsProperties(); Map<String, Object> network =
	 * RuntimeInfoWrapper.getNetworkProperties(); Map<String, Object> cpu = RuntimeInfoWrapper.getCpuProperties();
	 * Map<String, Object> memory = RuntimeInfoWrapper.getMemoryProperties(); Map<String, Object> streamsAgentCpuLoad =
	 * RuntimeInfoWrapper.getStreamsAgentCpuLoadProperties(); Map<String, Object> streamsAgentMemory =
	 * RuntimeInfoWrapper.getStreamsAgentMemoryProperties(); Map<String, Object> disc =
	 * RuntimeInfoWrapper.getDiscProperties(); Map<String, Object> versions =
	 * RuntimeInfoWrapper.getVersionsProperties(); Map<String, Object> configs =
	 * RuntimeInfoWrapper.getConfigsProperties(); Map<String, Object> service =
	 * RuntimeInfoWrapper.getServiceProperties();
	 * 
	 * RuntimeInfo runtimeInfo = new RuntimeInfo();
	 * 
	 * runtimeInfo.setOs(osMap); runtimeInfo.setNetwork(network); runtimeInfo.setCpu(cpu);
	 * runtimeInfo.setMemory(memory); runtimeInfo.setStreamsAgentCpu(streamsAgentCpuLoad);
	 * runtimeInfo.setStreamsAgentMemory(streamsAgentMemory); runtimeInfo.setDisk(disc);
	 * runtimeInfo.setVersions(versions); runtimeInfo.setConfigs(configs); runtimeInfo.setService(service);
	 * 
	 * ConfigData configData = new ConfigData<>(config, runtimeInfo);
	 * 
	 * String response = JobUtils.toJson(configData);
	 * 
	 * boolean wasSet = CuratorUtils.setData(path, response,
	 * CuratorSingleton.getSynchronizedCurator().getCuratorFramework());
	 * 
	 * if (!wasSet) { LoggerWrapper.addQuartzJobLog(this.getClass().getName(), path, response); } }
	 */

	private static RuntimeInfo getRuntime() {

		Map<String, Object> osMap = RuntimeInfoWrapper.getOsProperties();
		Map<String, Object> network = RuntimeInfoWrapper.getNetworkProperties();
		Map<String, Object> cpu = RuntimeInfoWrapper.getCpuProperties();
		Map<String, Object> memory = RuntimeInfoWrapper.getMemoryProperties();
		Map<String, Object> streamsAgentCpuLoad = RuntimeInfoWrapper.getStreamsAgentCpuLoadProperties();
		Map<String, Object> streamsAgentMemory = RuntimeInfoWrapper.getStreamsAgentMemoryProperties();
		Map<String, Object> disc = RuntimeInfoWrapper.getDiskProperties();
		Map<String, Object> versions = RuntimeInfoWrapper.getVersionsProperties();
		Map<String, Object> configs = RuntimeInfoWrapper.getConfigsProperties();
		Map<String, Object> service = RuntimeInfoWrapper.getServiceProperties();

		RuntimeInfo runtimeInfo = new RuntimeInfo();

		runtimeInfo.setOs(osMap);
		runtimeInfo.setNetwork(network);
		runtimeInfo.setCpu(cpu);
		runtimeInfo.setMemory(memory);
		runtimeInfo.setStreamsAgentCpu(streamsAgentCpuLoad);
		runtimeInfo.setStreamsAgentMemory(streamsAgentMemory);
		runtimeInfo.setDisk(disc);
		runtimeInfo.setVersions(versions);
		runtimeInfo.setConfigs(configs);
		runtimeInfo.setService(service);

		return runtimeInfo;
	}

	public static String agentRuntime() {
		MetadataProvider metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));

		Map<String, Object> uiMetadata = JsonUtils.jsonToMap(metadataProvider.getStreamAgentUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", AgentRuntimeUpdaterJob.getRuntime());

		return JsonUtils.objectToString(box);
	}

	public static String runtimeInformation() {
		MetadataProvider metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));

		Map<String, Object> uiMetadata = JsonUtils.jsonToMap(metadataProvider.getRuntimeUiMetadata(),
				new TypeReference<HashMap<String, Object>>() {
				});

		Map<String, Object> box = new HashMap<>();

		box.put("config", uiMetadata);
		box.put("data", AgentRuntimeUpdaterJob.getRuntime());

		return JsonUtils.objectToString(box);
	}

}
