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

package com.jkoolcloud.tnt4j.streams.registry.zoo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * The type Json rpc.
 */
public class JsonRpc {

	@JsonProperty("jsonrpc") // NON-NLS
	private String jsonrpc;
	@JsonProperty("method") // NON-NLS
	private String method;
	@JsonProperty("params") // NON-NLS
	private Map<String,Object> params = null;
	@JsonProperty("id") // NON-NLS
	private String id;

	/**
	 * Instantiates a new Json rpc.
	 *
	 * @param jsonrpc
	 *            the jsonrpc
	 * @param method
	 *            the method
	 * @param params
	 *            the params
	 * @param id
	 *            the id
	 */
	public JsonRpc(String jsonrpc, String method, Map<String,Object> params, String id) {
		this.jsonrpc = jsonrpc;
		this.method = method;
		this.params = params;
		this.id = id;
	}

	/**
	 * Instantiates a new Json rpc.
	 */
	public JsonRpc() {

	}

	/**
	 * Gets jsonrpc.
	 *
	 * @return the jsonrpc
	 */
	@JsonProperty("jsonrpc") // NON-NLS
	public String getJsonrpc() {
		return jsonrpc;
	}

	/**
	 * Sets jsonrpc.
	 *
	 * @param jsonrpc
	 *            the jsonrpc
	 */
	@JsonProperty("jsonrpc") // NON-NLS
	public void setJsonrpc(String jsonrpc) {
		this.jsonrpc = jsonrpc;
	}

	/**
	 * Gets method.
	 *
	 * @return the method
	 */
	@JsonProperty("method") // NON-NLS
	public String getMethod() {
		return method;
	}

	/**
	 * Sets method.
	 *
	 * @param method
	 *            the method
	 */
	@JsonProperty("method") // NON-NLS
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Gets params.
	 *
	 * @return the params
	 */
	@JsonProperty("params") // NON-NLS
	public Map<String,Object> getParams() {
		return params;
	}

	/**
	 * Sets params.
	 *
	 * @param params
	 *            the params
	 */
	@JsonProperty("params") // NON-NLS
	public void setParams(Map<String,Object> params) {
		this.params = params;
	}

	/**
	 * Gets id.
	 *
	 * @return the id
	 */
	@JsonProperty("id") // NON-NLS
	public String getId() {
		return id;
	}

	/**
	 * Sets id.
	 *
	 * @param id
	 *            the id
	 */
	@JsonProperty("id") // NON-NLS
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * The type Json rpc builder.
	 */
	public static final class JsonRpcBuilder {
		private String jsonrpc;
		private String method;
		private Map<String,Object> params = null;
		private String id;

		/**
		 * Instantiates a new Json rpc builder.
		 */
		public JsonRpcBuilder() {
		}

		/**
		 * Sets jsonrpc.
		 *
		 * @param jsonrpc
		 *            the jsonrpc
		 * @return the jsonrpc
		 */
		public JsonRpcBuilder setJsonrpc(String jsonrpc) {
			this.jsonrpc = jsonrpc;
			return this;
		}

		/**
		 * Sets method.
		 *
		 * @param method
		 *            the method
		 * @return the method
		 */
		public JsonRpcBuilder setMethod(String method) {
			this.method = method;
			return this;
		}

		/**
		 * Sets params.
		 *
		 * @param params
		 *            the params
		 * @return the params
		 */
		public JsonRpcBuilder setParams(Map<String,Object> params) {
			this.params = params;
			return this;
		}

		/**
		 * Sets id.
		 *
		 * @param id
		 *            the id
		 * @return the id
		 */
		public JsonRpcBuilder setId(String id) {
			this.id = id;
			return this;
		}

		/**
		 * Build json rpc.
		 *
		 * @return the json rpc
		 */
		public JsonRpc build() {
			return new JsonRpc(jsonrpc, method, params, id);
		}

	}
}