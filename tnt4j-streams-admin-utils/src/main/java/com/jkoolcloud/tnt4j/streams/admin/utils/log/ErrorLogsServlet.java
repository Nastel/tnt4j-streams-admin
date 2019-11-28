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

package com.jkoolcloud.tnt4j.streams.admin.utils.log;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * The type Error logs servlet.
 */
// TODO: merge both log servlets
public class ErrorLogsServlet extends HttpServlet {

	private StringBufferAppender appender;

	@Override
	public void init(ServletConfig config) throws ServletException {
		Logger logger = Logger.getRootLogger();
		appender = (StringBufferAppender) logger.getAppender("myErrorAppender");
		// appender.start();
	}

	@Override
	public void destroy() {
		// appender.stop() ;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (appender == null) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Appender not initialized");
		} else {
			PrintWriter pw = resp.getWriter();
			pw.print(appender.getLogs());
			resp.flushBuffer();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
