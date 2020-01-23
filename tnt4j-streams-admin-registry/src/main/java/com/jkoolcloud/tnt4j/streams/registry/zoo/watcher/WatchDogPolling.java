package com.jkoolcloud.tnt4j.streams.registry.zoo.watcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.xml.sax.SAXException;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.admin.utils.io.FileUtils;
import com.jkoolcloud.tnt4j.streams.custom.dirStream.DirWatchdog;
import com.jkoolcloud.tnt4j.streams.utils.LoggerUtils;

public class WatchDogPolling {

	private DirWatchdog dirWatchdog;
	private static final EventSink eventSink = LoggerUtils.getLoggerSink("StreamsAdmin_WatchDog");

	private StreamRegistry streamRegistry;
	private XPathWrapper xPathWrapper;

	// extract the filename after -f: flag e.g. ..\..\bin\tnt4j-streams.bat -f:bitcoin.xml -> bitcoin.xml
	private Pattern streamsScriptPattern = Pattern.compile("(?<=-f:)\\S*");

	private final String xmlGlobExp = "glob:**.xml";
	private final String winScriptGlob = "glob:**.bat";
	private final String linuxScriptGlob = "glob:**.sh";

	private final String os;

	public WatchDogPolling(String dirPath) {
		streamRegistry = new StreamRegistry();
		xPathWrapper = new XPathWrapper();
		dirWatchdog = new DirWatchdog(dirPath);
		os = System.getProperty("os.name").toLowerCase();
		try {
			registerAllResources(Paths.get(dirPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getStreamRegistry() {
		return streamRegistry.generateJsonSnapshot();
	}

	public void startStream(String streamName) {
		streamRegistry.startStream(streamName);
	}

	public void closeStream(String streamName) {
		streamRegistry.stopStream(streamName);
	}

	private boolean evaluateExpressionAgainstPath(Path path, String expression) {

		PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(expression);

		if (pathMatcher.matches(path)) {
			return true;
		}
		return false;
	}

	private void registerStreams(Path file) {
		List<String> streams = null;
		try (InputStream in = new FileInputStream(file.toFile())) {
			streams = xPathWrapper.executeExpression(in, "tnt-data-source/stream/@name");
		} catch (IOException | SAXException | XPathExpressionException e) {
			e.printStackTrace();
			eventSink.log(OpLevel.ERROR, "", e);
		}

		if (!(streams == null || streams.isEmpty())) {
			String key = generateKeyFromPath(file);
			streamRegistry.add(new Stream(file, streams));
		}
	}

	private String getRunScriptFileName(Path file) {
		System.out.println(file);
		String content = null;
		try {
			content = FileUtils.readFile(file.toString(), Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}

		Matcher matcher = streamsScriptPattern.matcher(content);

		if (matcher.find()) {
			return matcher.group();
		}

		return null;
	}

	private Path buildPathFromRunScript(Path file) {

		String matchedValue = getRunScriptFileName(file);

		if (matchedValue == null) {
			return null;
		}

		Path path = Paths.get(matchedValue);

		Path streamFile;
		if (path.isAbsolute()) {
			streamFile = path;
		} else {
			streamFile = file.getParent().resolve(matchedValue);
		}

		if (!Files.exists(streamFile)) {
			return null;
		}

		return streamFile;

	}

	private void registerStreamsByRunScript(Path file) {

		Path streamFile = buildPathFromRunScript(file);

		if (streamFile == null) {
			return;
		}

		List<String> streams = null;

		try (InputStream in = new FileInputStream(streamFile.toFile())) {
			streams = xPathWrapper.executeExpression(in, "tnt-data-source/stream/@name");
		} catch (IOException | SAXException | XPathExpressionException e) {
			e.printStackTrace();
			eventSink.log(OpLevel.ERROR, "", e);
		}

		if (!(streams == null || streams.isEmpty())) {
			String key = generateKeyFromPath(streamFile);
			streamRegistry.add(new Stream(streamFile, file, streams));
		}

	}

	public void registerAllResources(Path start) throws IOException {

		// TODO
		// if (start == null || !(Files.exists(start))) {
		// eventSink.log(OpLevel.ERROR, "", new IllegalArgumentException("bad start argument"));
		// throw new IllegalArgumentException("bad start argument");
		// }

		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return super.preVisitDirectory(dir, attrs);
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				if (os.startsWith("windows")) {

					if (evaluateExpressionAgainstPath(file, winScriptGlob)) {
						registerStreamsByRunScript(file);
					}

				} else if (os.startsWith("linux")) {

					if (evaluateExpressionAgainstPath(file, linuxScriptGlob)) {
						registerStreamsByRunScript(file);
					}

				}

				return super.visitFile(file, attrs);
			}
		});
	}

	public void run() {
		dirWatchdog.addObserverListener(new FileAlterationListener() {
			@Override
			public void onStart(FileAlterationObserver observer) {

			}

			@Override
			public void onDirectoryCreate(File directory) {

			}

			@Override
			public void onDirectoryChange(File directory) {

			}

			@Override
			public void onDirectoryDelete(File directory) {

			}

			@Override
			public void onFileCreate(File file) {
				registerStreams(file.toPath());
			}

			@Override
			public void onFileChange(File file) {
				registerStreams(file.toPath());

			}

			@Override
			public void onFileDelete(File file) {
				String key = generateKeyFromPath(file.toPath());
				streamRegistry.remove(key);
			}

			@Override
			public void onStop(FileAlterationObserver observer) {

			}
		});
	}

	public void start() throws Exception {
		dirWatchdog.start();
	}

	public void close() throws Exception {
		dirWatchdog.stop();
	}

	private String generateKeyFromPath(Path path) {
		int directoriesBack = 2;
		int subpathStart = path.getNameCount() - directoriesBack;
		return path.subpath(subpathStart, path.getNameCount()).toString();
	}
}
