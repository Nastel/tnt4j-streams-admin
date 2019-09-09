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

@SecureAction
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ActionRequestFilter implements ContainerRequestFilter {
	private static final String AUTHENTICATION_SCHEME = "Bearer";

	private boolean isTokenValid(String authorizationHeader) {
		String extractedToken = authorizationHeader.replace(AUTHENTICATION_SCHEME + " ", "");
		return Init.getActionToken().isIdentifierValid(extractedToken);

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
