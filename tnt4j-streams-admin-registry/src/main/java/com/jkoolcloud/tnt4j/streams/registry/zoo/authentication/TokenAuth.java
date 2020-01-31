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
