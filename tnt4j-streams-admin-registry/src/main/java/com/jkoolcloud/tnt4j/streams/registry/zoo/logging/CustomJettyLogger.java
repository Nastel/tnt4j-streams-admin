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

package com.jkoolcloud.tnt4j.streams.registry.zoo.logging;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

public class CustomJettyLogger extends ContainerLifeCycle implements RequestLog {

	@Override
	public void log(Request request, Response response) {

	}

	/*
	 * private static final EventSink streamsAdminLogger;
	 * 
	 * static { streamsAdminLogger = LoggerUtils.getLoggerSink("streamsAdminRestLog");
	 * streamsAdminLogger.setEventFormatter(new DefaultFormatter("{2}")); // NON-NLS }
	 * 
	 * @Override public void log(Request request, Response response) { streamsAdminLogger.log(OpLevel.INFO,
	 * request.toString()); }
	 */

}
