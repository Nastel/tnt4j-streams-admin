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

package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import com.codahale.metrics.jvm.ThreadDump;
import com.jkoolcloud.tnt4j.streams.registry.zoo.Init;
import com.jkoolcloud.tnt4j.streams.registry.zoo.streams.ZookeeperOutputStream;
import com.sun.management.OperatingSystemMXBean;

/**
 * The type Runtime information.
 */
public class RuntimeInformation {

	/**
	 * Gets java version.
	 *
	 * @return the java version
	 */
	public static String getJavaVersion() {
		return System.getProperty("java.version");
	}

	/**
	 * Gets os.
	 *
	 * @return the os
	 */
	public static String getOs() {
		return System.getProperty("os.name");
	}

	/**
	 * Gets base path.
	 *
	 * @return the base path
	 */
	public static String getBasePath() {
		return System.getProperty("user.dir");
	}

	public static String getOsArch() {
		return System.getProperty("os.arch");
	}

	public static String getOsVersion() {
		return System.getProperty("os.version");
	}

	public static String getJavaVmVersion() {
		return System.getProperty("java.vm.version");
	}

	public static String getHeapMemoryUsage() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		memoryMXBean.setVerbose(true);
		return memoryMXBean.getHeapMemoryUsage().toString();
	}

	public static String getNonHeapMemoryUsage() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		memoryMXBean.setVerbose(true);
		return memoryMXBean.getNonHeapMemoryUsage().toString();
	}

	public static Integer getAvailableCpuCount() {
		int cpuCount = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
		return cpuCount;
	}

	public static Integer getThreadCount() {
		return ManagementFactory.getThreadMXBean().getThreadCount();
	}

	public static Integer getPeakThreadCount() {
		return ManagementFactory.getThreadMXBean().getPeakThreadCount();
	}

	public static String getLog4jConfigPath() {
		return System.getProperty("log4j.configuration");
	}

	public static String getTnt4jLogPath() {
		return System.getProperty("tnt4j.config");
	}

	public static String getQuartzConfigPath() {
		return System.getProperty("quartz");
	}

	public static String getZkTreeConfigPath() {
		return System.getProperty("zkTree");
	}

	public static Double getSystemLoadAvg() {
		return ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
	}

	public static Long getSystemTotalDiskSpace() {
		return new File("/").getTotalSpace();
	}

	public static Long getSystemDiskFreeSpace() {
		return new File("/").getFreeSpace();
	}

	public static Long getSystemDiskUsedSpace() {
		return getSystemTotalDiskSpace() - getSystemDiskFreeSpace();
	}

	public static Long getJvmTotalPhysicalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	public static Long getJvmFreePhysicalMemory() {
		return Runtime.getRuntime().freeMemory();
	}

	public static Long getJvmMaxPhysicalMemory() {
		return Runtime.getRuntime().maxMemory();
	}

	public static Long getJvmUsedPhysicalMemory() {
		return getJvmTotalPhysicalMemory() - getJvmFreePhysicalMemory();
	}

	public static String getPublicIpAddress(String url) throws IOException {
		HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
		httpURLConnection.connect();

		InputStream inputStream = httpURLConnection.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		String ipAddr = bufferedReader.readLine();

		bufferedReader.close();
		httpURLConnection.disconnect();

		return ipAddr;
	}

	public static String getHostName() throws UnknownHostException {
		String hostName = InetAddress.getLocalHost().getHostName();
		return hostName;
	}

	public static String getHostAddress() throws UnknownHostException {
		String hostAddress = InetAddress.getLocalHost().getHostAddress();
		return hostAddress;
	}

	public static Double getSystemCpuLoad() {
		return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getSystemCpuLoad();
	}

	public static Double getProcessCpuLoad() {
		return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad();
	}

	public static Long getTotalPhysicalMemorySize() {
		return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
	}

	public static Long getFreePhysicalMemorySize() {
		return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
	}

	public static Long getUsedPhysicalMemorySize() {
		return getTotalPhysicalMemorySize() - getFreePhysicalMemorySize();
	}

	public static String getMainConfigPath() {
		String mainConfigPath = Init.getPaths().getMainConfigPath();

		return mainConfigPath;
	}

	public static String getStreamType(String classReference) throws ClassNotFoundException {
		Class<?> cls = Class.forName(classReference);

		return cls.getSuperclass().getSimpleName();
	}

	public static String getServiceName(String classReference) throws ClassNotFoundException {
		Class<?> cls = Class.forName(classReference);

		return cls.getSimpleName();
	}

	public static String getFullName(String classReference) throws ClassNotFoundException {
		Class<?> cls = Class.forName(classReference);

		return cls.getName();
	}

	public static String getSamplesPath(String mainConfigPath) {
		if (mainConfigPath == null || mainConfigPath.isEmpty()) {
			return "";
		}

		String samples = "samples";
		String samplesPath = mainConfigPath.substring(0, mainConfigPath.indexOf(samples) + samples.length());

		return samplesPath;
	}

	/**
	 * Gets thread dump.
	 *
	 * @return the thread dump
	 */
	public static String getThreadDump() {
		ThreadDump threadDump = new ThreadDump(ManagementFactory.getThreadMXBean());
		ZookeeperOutputStream zookeeperOutputStream = new ZookeeperOutputStream(new StringBuilder());
		threadDump.dump(zookeeperOutputStream);
		try {
			zookeeperOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String response = zookeeperOutputStream.getResponse();

		return response;
	}

}
