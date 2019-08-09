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
