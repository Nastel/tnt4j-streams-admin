package com.jkoolcloud.tnt4j.streams.registry.zoo.watcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jkoolcloud.tnt4j.core.OpLevel;
import com.jkoolcloud.tnt4j.streams.registry.zoo.logging.LoggerWrapper;

public class XPathWrapper {

	private XPath xPath;
	private DocumentBuilder documentBuilder;

	public XPathWrapper(XPath xPath, DocumentBuilder documentBuilder) {
		this.xPath = xPath;
		this.documentBuilder = documentBuilder;
	}

	public XPathWrapper() {
		xPath = XPathFactory.newInstance().newXPath();
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LoggerWrapper.logStackTrace(OpLevel.ERROR, e);
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
