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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FileUtils {

	private static Logger logger = LoggerFactory.getLogger(FileUtils.class);

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

	public static Manifest getJarInputStream(File file) {
		Manifest manifests = null;

		try (FileInputStream fileInputStream = new FileInputStream(file);
				JarInputStream jarInputStream = new JarInputStream(fileInputStream)) {

			manifests = jarInputStream.getManifest();

		} catch (IOException e) {
			// LoggerWrapper.addMessage(OpLevel.ERROR, e.getMessage());
			logger.error("", e);
		}
		return manifests;
	}

	public static Map<String, Object> getLibVersion(File[] files, List<String> libName) {

		Map<String, Object> searchResult = new HashMap<>();

		for (File file : files) {
			Manifest manifest = getJarInputStream(file);

			if (manifest == null) {
				continue;
			}

			java.util.jar.Attributes attribute = manifest.getMainAttributes();

			String name = attribute.getValue("Implementation-Title");

			if (name != null && libName.contains(name.toLowerCase())) {
				String value = attribute.getValue("Implementation-Version");
				searchResult.put(name, value);
			}
		}
		return searchResult;
	}

	public static File[] listAllFiles(String path) {
		File file = new File(path);

		return file.listFiles();
	}

	public static List<String> getParsersList(String path) throws Exception {
		List<String> parserNames = new ArrayList<>();

		DefaultHandler handler = new DefaultHandler();

		SAXParser saxParser = null;
		saxParser = SAXParserFactory.newInstance().newSAXParser();
		DefaultHandler defaultHandler = new DefaultHandler() {
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
				if (qName.equalsIgnoreCase("resource-ref")) {
					if (attributes.getValue("type").equalsIgnoreCase("Parser")) {
						parserNames.add(attributes.getValue("uri"));
					}
					handler.endDocument();
				}
			}
		};

		saxParser.parse(path, defaultHandler);

		return parserNames;
	}

	public static Map<String, Object> getStreamsAndClasses(String path)
			throws ParserConfigurationException, SAXException, IOException {
		Map<String, Object> streamToClassMap = new HashMap<>();

		SAXParser saxParser = null;
		saxParser = SAXParserFactory.newInstance().newSAXParser();
		DefaultHandler defaultHandler = new DefaultHandler() {
			@Override
			public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes)
					throws SAXException {
				if (qName.equalsIgnoreCase("stream")) {
					String name = attributes.getValue("name");
					String clazz = attributes.getValue("class");
					streamToClassMap.put(name, clazz);
				}
			}
		};
		saxParser.parse(path, defaultHandler);

		return streamToClassMap;
	}

	public static Map<String, Object> FileNameAndContentToMap(File file, String filenameKey, String fileContentKey) {
		String fileContent = null;
		String fileName = file.getName();
		try {

			fileContent = FileUtils.readFile(file.getPath().trim(), Charset.defaultCharset());
		} catch (IOException e) {
			// LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
			logger.error("", e);
		}
		Map<String, Object> response = new HashMap<>();
		response.put(filenameKey, fileName);
		response.put(fileContentKey, fileContent);

		return response;
	}

	public static List<Map<String, Object>> getConfigs(String path) {

		List<File> fileList = new ArrayList<>();
		FileUtils.listf(path, fileList);

		List<Map<String, Object>> list = new ArrayList<>();

		for (File file : fileList) {
			Map<String, Object> response = FileNameAndContentToMap(file, "name", "config");
			list.add(response);
		}

		return list;
	}

	public static List<Map<String, Object>> getConfigFilesSystemProp() {
		List<Map<String, Object>> list = new ArrayList<>();

		Set configFileNames = System.getProperties().keySet();

		for (Object configProperty : configFileNames) {
			String propertiesPath = System.getProperty((String) configProperty);
			if (propertiesPath.startsWith("file:")) {
				propertiesPath = propertiesPath.substring(5);
			}
			File file = new File(propertiesPath);
			if (file.isFile() && !configProperty.toString().equals("mainCfg")) {
				Map<String, Object> response = FileUtils.FileNameAndContentToMap(file, "name", "config");
				list.add(response);
			}
		}
		return list;
	}

	public static List<String> getAvailableFiles(String path) {
		List<File> fileList = new ArrayList<>();

		FileUtils.listf(path, fileList);

		List<String> fileNames = fileList.stream().map(file -> file.getName()).collect(Collectors.toList());

		return fileNames;
	}

	public static Properties getProperties(String path) {
		Properties properties = new Properties();
		try (FileInputStream fileInputStream = new FileInputStream(path)) {
			properties.load(fileInputStream);
		} catch (IOException e) {
			// LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
			logger.error("", e);
		}
		return properties;
	}

	public static String findFile(String dir, String fileName) {
		List<File> files = new ArrayList<>();

		FileUtils.listf(dir, files);

		for (File file : files) {
			if (file.getName().equals(fileName)) {
				return file.getAbsolutePath();
			}
		}
		return null;
	}

	public static byte[] compress(byte[] b, String name) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		ZipEntry entry = new ZipEntry(name);
		entry.setSize(b.length);
		zos.putNextEntry(entry);
		zos.write(b);
		zos.closeEntry();
		zos.close();
		return baos.toByteArray();
	}

	// Method tries to extract lib version from jar manifest.xml
	public static Map<String, Object> getLibsVersions(String path, List<String> libNames) {
		File[] files = listAllFiles(path);

		Map<String, Object> streamsVersionsMap = getLibVersion(files, libNames);

		return streamsVersionsMap;
	}

	/*
	 * public static JarInputStream getJarInputStream(File file) { FileInputStream fileInputStream = null;
	 * JarInputStream jarInputStream = null;
	 *
	 * try { fileInputStream = new FileInputStream(file); jarInputStream = new JarInputStream(fileInputStream);
	 *
	 * } catch (IOException e) { LoggerWrapper.addMessage(OpLevel.ERROR, e.getMessage()); } finally {
	 *
	 * try { fileInputStream.close(); } catch (IOException e) { LoggerWrapper.addMessage(OpLevel.ERROR, e.getMessage());
	 * } }
	 *
	 * return jarInputStream; }
	 *
	 * public static Map<String, Object> getLibVersion(File[] files, List<String> libName) {
	 *
	 * Map<String, Object> searchResult = new HashMap<>();
	 *
	 * for (File file : files) { JarInputStream jarInputStream = FileUtils.getJarInputStream(file); Manifest manifest =
	 * jarInputStream.getManifest();
	 *
	 * if (manifest == null) { continue; }
	 *
	 * java.util.jar.Attributes attribute = manifest.getMainAttributes();
	 *
	 * String name = attribute.getValue("Implementation-Title");
	 *
	 * if (name != null && libName.contains(name.toLowerCase())) { String value =
	 * attribute.getValue("Implementation-Version"); searchResult.put(name, value); } } return searchResult; }
	 */

}
