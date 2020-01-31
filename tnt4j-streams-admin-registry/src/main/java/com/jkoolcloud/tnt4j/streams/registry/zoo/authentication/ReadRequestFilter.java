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

package com.jkoolcloud.tnt4j.streams.registry.zoo.authentication;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.jkoolcloud.tnt4j.streams.registry.zoo.Init;

@SecureRead
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ReadRequestFilter implements ContainerRequestFilter {

	private static final String AUTHENTICATION_SCHEME = "Bearer";

	private boolean isTokenValid(String authorizationHeader) {
		String extractedToken = authorizationHeader.replace(AUTHENTICATION_SCHEME + " ", "");
		return Init.getReadToken().isIdentifierValid(extractedToken);

	}

	private boolean isCorrectAuthScheme(String authorizationHeader) {
		return authorizationHeader.startsWith(AUTHENTICATION_SCHEME);
	}

	@Override
	public void filter(ContainerRequestContext containerRequestContext) throws IOException {
		String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

		if (authorizationHeader == null || !isCorrectAuthScheme(authorizationHeader)
				|| !isTokenValid(authorizationHeader)) {
			containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			return;
		}

	}
}
