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

package com.jkoolcloud.tnt4j.streams.registry.zoo.watcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XPathWrapper {

	private XPath xPath;
	private DocumentBuilder documentBuilder;
	private Logger logger = LoggerFactory.getLogger("XPathWrapper");

	public XPathWrapper(XPath xPath, DocumentBuilder documentBuilder) {
		this.xPath = xPath;
		this.documentBuilder = documentBuilder;
	}

	public XPathWrapper() {
		xPath = XPathFactory.newInstance().newXPath();
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
			logger.error("", e);
		}
	}

	public List<String> executeExpression(InputStream in, String expr)
			throws IOException, SAXException, XPathExpressionException {

		Document doc = documentBuilder.parse(in);

		XPathExpression compiledExpr = xPath.compile(expr);

		NodeList nl = (NodeList) compiledExpr.evaluate(doc, XPathConstants.NODESET);

		doc.cloneNode(true);

		List<String> streamNames = new ArrayList<>();

		for (int i = 0; i < nl.getLength(); i++) {
			streamNames.add(nl.item(i).getNodeValue());
		}

		return streamNames;
	}

}
