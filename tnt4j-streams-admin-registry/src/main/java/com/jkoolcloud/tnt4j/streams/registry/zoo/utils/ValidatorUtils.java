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

package com.jkoolcloud.tnt4j.streams.registry.zoo.utils;

import java.io.File;

public class ValidatorUtils {

	public enum Resource {
		FILE, DIRECTORY
	}

	public static boolean isResourceAvailable(String path, Resource resource) {
		if (path == null) {
			return false;
		}

		File file = new File(path);

		switch (resource) {
		case FILE:
			if (file.isFile()) {
				return true;
			}
			break;
		case DIRECTORY:
			if (file.isDirectory()) {
				return true;
			}
			break;
		}
		return false;
	}

}
