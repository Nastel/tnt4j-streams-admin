package com.jkoolcloud.tnt4j.streams.registry.zoo.authentication;

import java.util.UUID;

public class TokenAuth implements Auth<String> {

	private String authToken;

	public TokenAuth(UUID uuid) {
		authToken = uuid.toString();
	}

	public TokenAuth() {
		authToken = UUID.randomUUID().toString();
	}

	@Override
	public String getIdentifier() {
		return authToken;
	}

	@Override
	public boolean isIdentifierValid(String token) {
		return authToken.equals(token);
	}
}
