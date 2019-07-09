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

package com.jkoolcloud.tnt4j.streams.admin.backend;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.jkoolcloud.tnt4j.streams.admin.backend.data.JKoolData;
import com.jkoolcloud.tnt4j.streams.admin.backend.data.LogData;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;

/**
 * The type Service info endpoint.
 */
@ApplicationScoped
@Path("/health_services")
public class ServiceInfoEndpoint extends HttpServlet {

	// private static Logger LOG = LoggerFactory.getLogger(ServiceInfoEndpoint.class);

	@Inject
	private ServiceData readAndParseData;

	/**
	 * Do get repository info response.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the response
	 */
	@GET
	@Path("/{serviceName}/repository")
	@Produces(ClsConstants.MIME_TYPE_JSON)
	public Response doGetRepositoryInfo(@PathParam("serviceName") String serviceName) {
		String incompleteBlocks = JKoolData.getDataFromRepository(serviceName);
		return Response.status(200).entity(incompleteBlocks).build();
	}

	/**
	 * Do get incomplete blocks info response.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the response
	 */
	@GET
	@Path("/{serviceName}/incomplete")
	@Produces(ClsConstants.MIME_TYPE_JSON)
	public Response doGetIncompleteBlocksInfo(@PathParam("serviceName") String serviceName) {
		String incompleteBlocks = JKoolData.getDataFromIncompleteBlocks(serviceName);
		return Response.status(200).entity(incompleteBlocks).build();
	}

	/**
	 * Do get incomplete blocks no receipt response.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the response
	 */
	@GET
	@Path("/{serviceName}/incompleteNoReceipt")
	@Produces(ClsConstants.MIME_TYPE_JSON)
	public Response doGetIncompleteBlocksNoReceipt(@PathParam("serviceName") String serviceName) {
		String incompleteBlocks = JKoolData.getDataFromIncompleteBlocksNoReceipt(serviceName);
		return Response.status(200).entity(incompleteBlocks).build();
	}

	/**
	 * Do get log info response.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the response
	 * @throws IOException
	 *             the io exception
	 */
	@GET
	@Path("/{serviceName}/log")
	@Produces(ClsConstants.MIME_TYPE_JSON)
	public Response doGetLogInfo(@PathParam("serviceName") String serviceName) throws IOException {
		String log = LogData.getDataFromLogs(serviceName);
		return Response.status(200).entity(log).build();
	}

	/**
	 * Do get err log info response.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the response
	 * @throws IOException
	 *             the io exception
	 */
	@GET
	@Path("/{serviceName}/errLog")
	@Produces(ClsConstants.MIME_TYPE_JSON)
	public Response doGetErrLogInfo(@PathParam("serviceName") String serviceName) throws IOException {
		String errLog = LogData.getDataFromErrorLogs(serviceName);
		return Response.status(200).entity(errLog).build();
	}

	/**
	 * Do get service info all zoo keeper response.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the response
	 * @throws Exception
	 *             the exception
	 */
	@GET
	@Path("/{serviceName}/all")
	@Produces(ClsConstants.MIME_TYPE_JSON)
	public Response doGetServiceInfoAllZooKeeper(@PathParam("serviceName") String serviceName) throws Exception {
		String jsonServiceData = readAndParseData.serviceInfoParseToJSONFromAllData(serviceName);
		return Response.status(200).entity(jsonServiceData).build();
	}

	/**
	 * Do get service info response.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the response
	 * @throws Exception
	 *             the exception
	 */
	@GET
	@Path("/{serviceName}")
	@Produces(ClsConstants.MIME_TYPE_JSON)
	public Response doGetServiceInfo(@PathParam("serviceName") String serviceName) throws Exception {
		String jsonServiceData = readAndParseData.serviceInfoParseToJSON(serviceName, false);
		return Response.status(200).entity(jsonServiceData).build();
	}

	/**
	 * Do get service info zoo keeper response.
	 *
	 * @param serviceName
	 *            the service name
	 * @return the response
	 * @throws Exception
	 *             the exception
	 */
	@GET
	@Path("/{serviceName}/data")
	@Produces(ClsConstants.MIME_TYPE_JSON)
	public Response doGetServiceInfoZooKeeper(@PathParam("serviceName") String serviceName) throws Exception {
		String jsonServiceData = readAndParseData.serviceInfoParseToJSONZooKeeperData(serviceName);
		return Response.status(200).entity(jsonServiceData).build();
	}

	/**
	 * Do get service info response.
	 *
	 * @return the response
	 * @throws Exception
	 *             the exception
	 */
	@GET
	@Path("/all")
	@Produces(ClsConstants.MIME_TYPE_JSON)
	public Response doGetServiceInfo() throws Exception {
		String finalData = readAndParseData.readAllData();
		return Response.status(200).entity(finalData).build();
	}

	/**
	 * Sets service data.
	 *
	 * @param readAndParseData
	 *            the read and parse data
	 */
	public void setServiceData(ServiceData readAndParseData) {
		this.readAndParseData = readAndParseData;
	}
}