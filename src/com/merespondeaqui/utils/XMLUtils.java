package com.merespondeaqui.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLUtils {

	public static Node findNode(Node parentNode, String key) {
		NodeList childNodes = parentNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeName().equals(key)) {
				return childNode;
			}
		}
		return null;
	}

	public static Document createDocument(String xmlString) throws SAXException,
			IOException, ParserConfigurationException,
			UnsupportedEncodingException {
		Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				new ByteArrayInputStream(xmlString.getBytes("UTF8")));
		return xmlDocument;
	}

	public static Node findNodeByClass(Node parentNode, String key) {
		NodeList childNodes = parentNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getAttributes().getNamedItem("class").getNodeValue().equals(key)) {
				return childNode;
			}
		}
		return null;
	}

}
