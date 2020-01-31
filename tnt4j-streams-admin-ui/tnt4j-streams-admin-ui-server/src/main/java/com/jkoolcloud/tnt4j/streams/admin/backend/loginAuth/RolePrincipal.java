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

package com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth;

import java.security.Principal;

/**
 * Holds a single role name that the user is in
 *
 * @author sixthpoint
 */
public class RolePrincipal implements Principal {

	private String name;

	/**
	 * Initializer
	 *
	 * @param name
	 */
	public RolePrincipal(String name) {
		super();
		this.name = name;
	}

	/**
	 * Get the role name
	 *
	 * @return
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the role name
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
}