package com.jkoolcloud.tnt4j.streams.registry.zoo.watcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class Stream {

	private Path file;
	private Path runScript;
	private List<String> streams;
	private Process process = null;
	private boolean running;

	public Stream(Path file, List<String> streams) {
		this.file = file;
		this.streams = streams;
	}

	public Stream(Path file, Path runScript, List<String> streams) {
		this.file = file;
		this.runScript = runScript;
		this.streams = streams;
	}

	public String getStreamFile() {
		return file.toString();
	}

	public String getDirectoryName() {
		return file.getParent().toString();
	}

	public Path getDirectoryPath() {
		return file.getParent();
	}

	public List<String> getStreams() {
		return streams;
	}

	public void setFile(Path file) {
		this.file = file;
	}

	public Path getRunScript() {
		return runScript;
	}

	public void setRunScript(Path runScript) {
		this.runScript = runScript;
	}

	public void setStreams(List<String> streams) {
		this.streams = streams;
	}

	public boolean isRunning() {
		return running;
	}

	public void start() {
		String runScript = getRunScript().toString();
		String workingDir = getDirectoryPath().toString();

		// TODO linux support
		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", runScript).directory(new File(workingDir));

		try {
			process = pb.start();
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\":\" process could not be started\" }").build());
		}

		running = process.isAlive();
	}

	public void close() {
		process.destroy();
		running = process.isAlive();
	}

}
