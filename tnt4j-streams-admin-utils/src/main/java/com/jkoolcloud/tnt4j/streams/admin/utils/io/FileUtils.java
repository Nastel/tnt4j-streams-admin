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

package com.jkoolcloud.tnt4j.streams.admin.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * The type File utils.
 */
public class FileUtils {

	/**
	 * Listf.
	 *
	 * @param directoryName
	 *            the directory name
	 * @param files
	 *            the files
	 */
	public static void listf(String directoryName, List<File> files) {
		File directory = new File(directoryName);

		File[] fList = directory.listFiles();
		if (fList != null) {
			for (File file : fList) {
				if (file.isFile()) {
					files.add(file);
				} else if (file.isDirectory()) {
					listf(file.getAbsolutePath(), files);
				}
			}
		}
	}

	/**
	 * Read file string.
	 *
	 * @param path
	 *            the path
	 * @param encoding
	 *            the encoding
	 * @return the string
	 * @throws IOException
	 *             the io exception
	 */
	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	/**
	 * Load properties wrapper properties.
	 *
	 * @param path
	 *            the path
	 * @return the properties
	 */
	public static Properties loadPropertiesWrapper(String path) {

		File file = new File(path);

		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return properties;
	}

}
