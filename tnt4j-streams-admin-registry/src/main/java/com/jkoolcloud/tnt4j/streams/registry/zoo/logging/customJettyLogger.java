package com.jkoolcloud.tnt4j.streams.registry.zoo.logging;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.format.DefaultFormatter;
import com.jkoolcloud.tnt4j.sink.EventSink;
import com.jkoolcloud.tnt4j.streams.utils.LoggerUtils;

public class customJettyLogger extends ContainerLifeCycle implements RequestLog {

	private static final EventSink streamsAdminLogger;

	static {
		streamsAdminLogger = LoggerUtils.getLoggerSink("streamsAdminRestLog");
		streamsAdminLogger.setEventFormatter(new DefaultFormatter("{2}")); // NON-NLS
	}

	@Override
	public void log(Request request, Response response) {
		streamsAdminLogger.log(OpLevel.INFO, request.toString());
	}
}
