package com.jkoolcloud.tnt4j.streams.registry.zoo.jmx;

import java.io.File;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.registry.zoo.Init;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.FileUtils;
import com.jkoolcloud.tnt4j.streams.utils.LoggerUtils;
import com.sun.management.OperatingSystemMXBean;

public class JmxStatistics {

	private MBeanServerConnection mbeanConn;

	private Map<String, Object> objectNameToMXBean = new HashMap<>();

	private static final EventSink JmxStatisticsEventSink = LoggerUtils.getLoggerSink("JmxStatistics");

	public JmxStatistics(MBeanServerConnection mbeanConn) {
		this.mbeanConn = mbeanConn;
	}

	private Object requestMXBean(String objName, Class cls) {
		ObjectName objectName = null;

		try {
			objectName = new ObjectName(objName);
		} catch (MalformedObjectNameException e) {
			JmxStatisticsEventSink.log(OpLevel.ERROR, "Object name string was incorrect or malformed", e);
			return null;
		}

		Object mxBean = objectNameToMXBean.get(objName);

		if (mxBean == null) {
			mxBean = JMX.newMXBeanProxy(mbeanConn, objectName, cls, true);
			objectNameToMXBean.put(objName, mxBean);
		}

		return mxBean;
	}

	public Map<String, Object> getThreadInfo() {

		ThreadMXBean threadMXBean = (ThreadMXBean) requestMXBean("java.lang:type=Threading", ThreadMXBean.class);

		Map<String, Object> streamsAgentCpuLoadMap = new HashMap<>();

		Integer threadCount = threadMXBean.getThreadCount();
		// Double processCpuLoad = RuntimeInformation.getProcessCpuLoad();
		Integer peakThreadCount = threadMXBean.getPeakThreadCount();

		streamsAgentCpuLoadMap.put("Process CPU load [0-1]", 0);
		streamsAgentCpuLoadMap.put("Thread count", threadCount);
		streamsAgentCpuLoadMap.put("Peak thread count", peakThreadCount);

		return streamsAgentCpuLoadMap;

	}

	public Map<String, Object> getStreamsAgentMemoryProperties() {
		MemoryMXBean memoryMXBean = (MemoryMXBean) requestMXBean("java.lang:type=Memory", MemoryMXBean.class);

		MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
		MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

		Map<String, Object> streamsAgentMemoryInfo = new HashMap<>();
		streamsAgentMemoryInfo.put("Total JVM RAM memory (bytes)", heapMemoryUsage.getCommitted());
		streamsAgentMemoryInfo.put("Free JVM RAM memory (bytes)", heapMemoryUsage.getMax() - heapMemoryUsage.getUsed());
		streamsAgentMemoryInfo.put("used JVM RAM memory (bytes)", heapMemoryUsage.getUsed());
		streamsAgentMemoryInfo.put("Max JVM RAM memory (bytes)", heapMemoryUsage.getMax());

		streamsAgentMemoryInfo.put("Heap memory usage", heapMemoryUsage.toString());
		streamsAgentMemoryInfo.put("Non heap memory usage", nonHeapMemoryUsage.toString());

		return streamsAgentMemoryInfo;
	}

	public Map<String, Object> getCpuInfo() {

		OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) requestMXBean(
				"java.lang:type=OperatingSystem", OperatingSystemMXBean.class);

		if (operatingSystemMXBean == null) {
			return new HashMap<>();
		}

		Map<String, Object> cpuInfo = new HashMap<>();
		cpuInfo.put("Available CPU count", operatingSystemMXBean.getAvailableProcessors());
		cpuInfo.put("System CPU load [0-1]", operatingSystemMXBean.getSystemCpuLoad());

		return cpuInfo;
	}

	public Map<String, Object> getConfigs() {
		RuntimeMXBean runtimeMXBean = (RuntimeMXBean) requestMXBean("java.lang:type=Runtime", RuntimeMXBean.class);

		if (runtimeMXBean == null) {
			return new HashMap<>();
		}

		Map<String, String> systemProperties = runtimeMXBean.getSystemProperties();

		String tnt4jPath = systemProperties.get("tnt4j.config");
		String log4jPath = systemProperties.get("log4j.configuration");
		String mainConfigPath = Init.getPaths().getMainConfigPath();
		String streamConfigs = Init.getPaths().getSampleCfgsPath();

		Map<String, Object> configsLocations = new HashMap<>();

		configsLocations.put("Log4j config path", log4jPath);
		configsLocations.put("tnt4j.config path", tnt4jPath);
		configsLocations.put("Main stream agent config path", mainConfigPath);
		configsLocations.put("Stream configs (samples)", streamConfigs);

		return configsLocations;
	}

	public Map<String, Object> getRam() {
		OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) requestMXBean(
				"java.lang:type=OperatingSystem", OperatingSystemMXBean.class);

		if (operatingSystemMXBean == null) {
			return new HashMap<>();
		}

		long freePhysicalMemorySize = operatingSystemMXBean.getFreePhysicalMemorySize();
		long totalPhysicalMemorySize = operatingSystemMXBean.getTotalPhysicalMemorySize();

		Map<String, Object> physicalMemory = new HashMap<>();
		physicalMemory.put("Free system RAM (bytes)", freePhysicalMemorySize);
		physicalMemory.put("Total system RAM (bytes)", totalPhysicalMemorySize);
		physicalMemory.put("Used system RAM (bytes)", totalPhysicalMemorySize - freePhysicalMemorySize);

		return physicalMemory;
	}

	public Map<String, Object> getDisk() {
		long totalSpace = new File("/").getTotalSpace();
		long freeSpace = new File("/").getFreeSpace();
		long usedSpace = totalSpace - freeSpace;

		Map<String, Object> disk = new HashMap<>();

		disk.put("Free disk space (bytes)", freeSpace);
		disk.put("Used disk space (bytes)", usedSpace);
		disk.put("Total disk space (bytes)", totalSpace);

		return disk;
	}

	public Map<String, Object> getOsInfo() {
		OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) requestMXBean(
				"java.lang:type=OperatingSystem", OperatingSystemMXBean.class);

		String name = operatingSystemMXBean.getName();
		String arch = operatingSystemMXBean.getArch();
		String version = operatingSystemMXBean.getVersion();

		Map<String, Object> osInfo = new HashMap<>();

		osInfo.put("OS", name);
		osInfo.put("OS architecture", arch);
		osInfo.put("OS version", version);

		return osInfo;
	}

	public Map<String, Object> getNetworkInfo() {

		Map<String, Object> networkInfo = new HashMap<>();

		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostName = "UnknownHost";
		}

		String hostAddress = null;

		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			hostAddress = "UnknownAddress";
		}

		networkInfo.put("Hostname", hostName);
		networkInfo.put("Host address", hostAddress);

		return networkInfo;

	}

	public Map<String, Object> getVersionsProperties() {

		RuntimeMXBean runtimeMXBean = (RuntimeMXBean) requestMXBean("java.lang:type=Runtime", RuntimeMXBean.class);

		if (runtimeMXBean == null) {
			return new HashMap<>();
		}

		Map<String, String> systemProperties = runtimeMXBean.getSystemProperties();

		String javaVersion = systemProperties.get("java.version");
		String javaVmVersion = systemProperties.get("java.vm.version");

		Map<String, Object> streamVersionsMap = FileUtils.getLibsVersions(Init.getPaths().getLibrariesPath(),
				Arrays.asList("tnt4j", "tnt4j-streams-core", "jesl"));

		Map<String, Object> versions = new HashMap<>();
		versions.put("TNT4J version", streamVersionsMap.get("tnt4j"));
		versions.put("JESL version", streamVersionsMap.get("jesl"));
		versions.put("Streams Core version", streamVersionsMap.get("tnt4j-streams-core"));
		versions.put("Java version", javaVersion);
		versions.put("Java VM version", javaVmVersion);

		return versions;
	}

}
