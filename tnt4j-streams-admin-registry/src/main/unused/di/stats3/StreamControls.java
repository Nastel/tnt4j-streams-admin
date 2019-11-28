package com.jkoolcloud.tnt4j.streams.registry.zoo.stats;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.StreamsAgent;
import com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils;
import com.jkoolcloud.tnt4j.streams.custom.dirStream.DirStreamingManager;
import com.jkoolcloud.tnt4j.streams.custom.dirStream.StreamingJob;
import com.jkoolcloud.tnt4j.streams.custom.dirStream.StreamingJobListener;
import com.jkoolcloud.tnt4j.streams.custom.dirStream.StreamingJobLogger;
import com.jkoolcloud.tnt4j.streams.inputs.StreamingStatus;
import com.jkoolcloud.tnt4j.streams.inputs.TNTInputStreamStatistics;
import com.jkoolcloud.tnt4j.streams.registry.zoo.configuration.MetadataProvider;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;
import com.jkoolcloud.tnt4j.streams.registry.zoo.utils.StringUtils;

public class StreamControls {

	private static MetadataProvider metadataProvider = new MetadataProvider(System.getProperty("streamsAdmin"));

	private static final String FILE_WILDCARD_NAME = "streams_job_*.xml";

	public static void restartStreams(String streamName) {

		String mainCfgPath = metadataProvider.getMainCfgPath();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(mainCfgPath));
		} catch (FileNotFoundException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		try {
			StreamsAgent.restartStreams(bufferedReader, streamName);
		} catch (Exception e) {
			e.printStackTrace();
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}
	}

	public static void stopStream(String streamName) {
		try {
			StreamsAgent.stopStreams(streamName);
		} catch (Exception e) {
			e.printStackTrace();
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

	}

	public static void dirStreaming(String dirToMonitor) throws Exception {
		DirStreamingManager dm = new DirStreamingManager(dirToMonitor, FILE_WILDCARD_NAME);

		dm.setTnt4jCfgFilePath(System.getProperty("tnt4j.config")); // NON-NLS
		dm.addStreamingJobListener(new StreamingJobLogger());

		dm.addStreamingJobListener(new StreamingJobListener() {
			@Override
			public void onProgressUpdate(StreamingJob job, int current, int total) {

			}

			@Override
			public void onSuccess(StreamingJob job) {
			}

			@Override
			public void onFailure(StreamingJob job, String msg, Throwable exc, String code) {

			}

			@Override
			public void onStatusChange(StreamingJob job, StreamingStatus status) {

			}

			@Override
			public void onFinish(StreamingJob job, TNTInputStreamStatistics stats) {
				String fileToBeDeleted = StreamControls.FILE_WILDCARD_NAME.replace("*", job.getJobId().toString());

				String pathToFile = dirToMonitor + "/" + fileToBeDeleted;

				File file = new File(pathToFile);

				if (file.isFile()) {
					boolean isDeleted = file.delete();

					if (!isDeleted) {
						LoggerWrapper.addMessage(OpLevel.INFO, String
								.format("File >>%s<< at path >>%s<< was not deleted", fileToBeDeleted, pathToFile));
					}

				} else {
					LoggerWrapper.addMessage(OpLevel.INFO,
							String.format("File >>%s<< at path >>%s<< was not found", fileToBeDeleted, pathToFile));
				}

			}

			@Override
			public void onStreamEvent(StreamingJob job, OpLevel level, String message, Object source) {

			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("JVM exiting!..."); // NON-NLS
				synchronized (dm) {
					dm.notify();

				}
				dm.stop();
			}
		}));
		dm.start();
		synchronized (dm) {
			dm.wait();
		}
	}

	private static String prepareTemplate(Map<String, Object> placeholderToValue, String templatePath) {
		String template = null;
		try {
			template = FileUtils.readFile(templatePath, Charset.defaultCharset());
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		return StringUtils.substitutePlaceholders(template, placeholderToValue);
	}

	private static String saveConfig(String config, String dirPath, String cfgName) {
		Path pathToConfig = null;
		try {
			pathToConfig = Files.write(Paths.get(dirPath + "/" + cfgName), config.getBytes(), CREATE, WRITE);
		} catch (IOException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
		}

		return pathToConfig.toString();
	}

	public static void processRequest(String[] blocksArr) {
		String streamTemplatePath = metadataProvider.getReplayTemplatePath();

		String userRequests = metadataProvider.getMonitoredFolder();

		if (streamTemplatePath == null || streamTemplatePath.isEmpty()) {
			LoggerWrapper.addMessage(OpLevel.ERROR, "No template found");
			return;
		}

		if (userRequests == null || userRequests.isEmpty()) {
			LoggerWrapper.addMessage(OpLevel.ERROR, "No userRequests folder found");
			return;
		}

		String uuid = UUID.randomUUID().toString();

		Map<String, Object> streamPlaceholderToValue = new HashMap<>();
		streamPlaceholderToValue.put("blocks", Arrays.asList(blocksArr));
		streamPlaceholderToValue.put("UID", uuid);

		String config = prepareTemplate(streamPlaceholderToValue, streamTemplatePath);

		saveConfig(config, userRequests, FILE_WILDCARD_NAME.replace("*", uuid));
	}

}
