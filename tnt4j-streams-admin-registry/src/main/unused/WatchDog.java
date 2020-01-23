package com.jkoolcloud.tnt4j.streams.registry.zoo.watcher;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.utils.LoggerUtils;

public class WatchDog {

	private static final EventSink eventSink = LoggerUtils.getLoggerSink("StreamsAdmin_WatchDog");

	private StreamRegistry streamRegistry;
	private XPathWrapper xPathWrapper;
	private Map<String, WatchKey> nameToKey;
	private WatchService watchService;

	public WatchDog() {
		streamRegistry = new StreamRegistry();
		xPathWrapper = new XPathWrapper();
		nameToKey = new HashMap<>();
		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			WatchDog.eventSink.log(OpLevel.ERROR, "", e);
		}
	}

	public void registerDirectory(Path path) throws IOException {
		WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

		nameToKey.put(path.toAbsolutePath().toString(), watchKey);
	}

	public String getStreamRegistry() {
		return streamRegistry.generateJsonSnapshot();
	}

	public void deRegister(String name) {
		WatchKey key = nameToKey.get(name);

		if (key != null) {
			key.cancel();
		}
	}

	public void registerAllResources(Path start) throws IOException {

		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				registerDirectory(dir);
				return super.preVisitDirectory(dir, attrs);
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.xml");

				if (pathMatcher.matches(file)) {
					registerStreams(file);
				}

				return super.visitFile(file, attrs);
			}
		});

	}

	public void run() throws InterruptedException {
		WatchKey key;

		while ((key = watchService.take()) != null) {

			if (!key.isValid()) {
				System.out.println("key is invalid");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent<Path> ev = (WatchEvent<Path>) event;

				Path directory = (Path) key.watchable();
				Path absolute = directory.resolve(ev.context());

				String eventName = event.kind().name();
				if (eventName.equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
					onCreate(absolute);
				} else if (eventName.equals(StandardWatchEventKinds.ENTRY_MODIFY.name())) {
					onModify(absolute);
				} else if (eventName.equals(StandardWatchEventKinds.ENTRY_DELETE.name())) {
					onDelete(absolute);
					System.out.println(getStreamRegistry());
				}

				System.out.println(streamRegistry.generateJsonSnapshot());
			}
			key.reset();
		}
	}

	private void onCreate(Path absolute) {
		System.out.println("onCreate");
		System.out.println(absolute);
		if (Files.isRegularFile(absolute)) {
			registerStreams(absolute);
		} else if (Files.isDirectory(absolute)) {
			// TODO let the caller handle?
			try {
				registerDirectory(absolute);
			} catch (IOException e) {
				WatchDog.eventSink.log(OpLevel.ERROR, "", e);
			}
		}
	}

	private void onDelete(Path absolute) {
		System.out.println("onDelete");
		System.out.println(absolute);
		String key = generateKeyFromPath(absolute);
		streamRegistry.remove(key);
	}

	private void onModify(Path absolute) {
		System.out.println("onFileModify");
		System.out.println(absolute);

		if (Files.isRegularFile(absolute)) {

		} else if (Files.isDirectory(absolute)) {

		}
	}

	private String generateKeyFromPath(Path path) {
		int directoriesBack = 2;
		int subpathStart = path.getNameCount() - directoriesBack;
		return path.subpath(subpathStart, path.getNameCount()).toString();
	}

	private void registerStreams(Path file) {
		List<String> streams = null;
		try (InputStream in = new FileInputStream(file.toFile())) {
			streams = xPathWrapper.executeExpression(in, "tnt-data-source/stream/@name");
		} catch (IOException | SAXException | XPathExpressionException e) {
			WatchDog.eventSink.log(OpLevel.ERROR, "", e);
		}

		if (!(streams == null || streams.isEmpty())) {
			String key = generateKeyFromPath(file);
			streamRegistry.add(new Stream(file, streams));
		}
	}

}

/*
 * 
 * System.out.println(changed); // OGG - Copy - Copy (3).txt System.out.println(changed.toAbsolutePath()); //
 * C:\development\tnt4j\OGG - Copy - Copy (3).txt System.out.println(event.toString()); //
 * sun.nio.fs.AbstractWatchKey$Event@179d3b25 System.out.println(event.context().toString()); // OGG - Copy - Copy
 * (3).txt System.out.println(event.kind().name()); // ENTRY_CREATE
 * 
 * System.out.println("event"); System.out.println("------------------");
 */

/*
 * private void onDirectoryCreate(WatchEvent<?> event) { Path changed = (Path) event.context();
 * System.out.println("onDirectoryCreate "); }
 *
 * private void onDirectoryDelete(WatchEvent<?> event) { Path changed = (Path) event.context();
 * System.out.println("onDirectoryDelete");
 *
 * }
 *
 * private void onDirectoryModify(WatchEvent<?> event) { Path changed = (Path) event.context();
 * System.out.println("onDirectoryModify ");
 *
 * }
 *
 * private void onFileCreate(WatchEvent<?> event) { Path changed = (Path) event.context();
 * System.out.println("onFileCreate "); }
 *
 * private void onFileDelete(WatchEvent<?> event) { Path changed = (Path) event.context();
 * System.out.println("onFileDelete"); }
 *
 * private void onFileModify(WatchEvent<?> event) { Path changed = (Path) event.context();
 * System.out.println("onFileModify"); }
 */