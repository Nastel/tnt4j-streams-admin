/*
 * Copyright 2014-2020 JKOOL, LLC.
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
