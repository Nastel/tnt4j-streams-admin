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

package com.jkoolcloud.tnt4j.streams.registry.zoo.streams;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Zookeeper output stream.
 */
public class ZookeeperOutputStream extends OutputStream {
	// private static final EventSink LOGGER_ZOOKEEPER = LoggerUtils.getLoggerSink("zookeeperLog"); // NON-NLS

	private Logger logger = LoggerFactory.getLogger(ZookeeperOutputStream.class);

	/**
	 * The Message buffer.
	 */
	StringBuilder messageBuffer;

	/**
	 * Instantiates a new Zookeeper output stream.
	 *
	 * @param messageBuffer
	 *            the message buffer
	 */
	public ZookeeperOutputStream(StringBuilder messageBuffer) {
		this.messageBuffer = messageBuffer;
		// LOGGER_ZOOKEEPER.setEventFormatter(new DefaultFormatter("{2}")); // NON-NLS
	}

	/**
	 * Writes the specified byte to this output stream. The general contract for {@code write} is that one byte is
	 * written to the output stream. The byte to be written is the eight low-order bits of the argument {@code b}. The
	 * 24 high-order bits of {@code b} are ignored.
	 * <p>
	 * Subclasses of {@link OutputStream} must provide an implementation for this method.
	 *
	 * @param b
	 *            the {@code byte}.
	 * @throws IOException
	 *             if an I/O error occurs. In particular, an {@link IOException} may be thrown if the output stream has
	 *             been closed.
	 */
	@Override
	public void write(int b) throws IOException {
		char messageChar = (char) b;
		messageBuffer.append(messageChar);
	}

	/**
	 * Gets response.
	 *
	 * @return the response
	 */
	public String getResponse() {
		return messageBuffer.toString();
	}

}
