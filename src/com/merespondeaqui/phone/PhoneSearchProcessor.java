package com.merespondeaqui.phone;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.auth.BasicAuthorization;

import com.merespondeaqui.Processor;
import com.merespondeaqui.TwitterUtils;
import com.merespondeaqui.Utils;

public class PhoneSearchProcessor implements Processor {

	private static final String PREFIX = "qualotelefone";
	private static final String FULL_PREFIX = Utils.createFullPrefix(PREFIX);
	
	private DefaultHttpClient httpClient;
	private String authHeader;
	private String bitlyUser;
	private String bitlyAPIKey;
	
	public PhoneSearchProcessor(Properties properties) {
		String consumerKey = properties.getProperty("apontador.consumerkey");
		String consumerSecret = properties.getProperty("apontador.consumersecret");
		this.bitlyUser = properties.getProperty("bitly.username");
		this.bitlyAPIKey = properties.getProperty("bitly.apikey");
		
		this.authHeader = new BasicAuthorization(consumerKey, consumerSecret)
				.getAuthorizationHeader(null);
		this.httpClient = new DefaultHttpClient();
	}

	@Override
	public void process(Tweet tweet, Twitter twitter) throws Exception {
		
		String text = tweet.getText();
		
		String[] splitText = text.split("\\s+");
		String place = text.substring(FULL_PREFIX.length() + splitText[2].length() + 2);
		
		HttpGet httpget = new HttpGet("http://api.apontador.com.br/v1/search/places?q=" + URLEncoder.encode(place, "UTF-8"));
		httpget.setHeader("Authorization", authHeader);
		
		HttpResponse httpResponse = httpClient.execute(httpget);
		
		String xmlString = IOUtils.toString(httpResponse.getEntity().getContent());
		Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				new ByteArrayInputStream(xmlString.getBytes("UTF8")));
		NodeList places = xmlDocument.getElementsByTagName("place");
		
		if (places.getLength() == 0) {
			TwitterUtils.reply("Não foi encontrado estabelecimento para a busca '" + place + "'", 
					tweet, twitter);
			return;
		}
		
		Node placeNode = places.item(0);
		
		String name = findNode(placeNode, "name").getTextContent();
		String url = findNode(placeNode, "main_url").getTextContent();
		
		Node phoneNode = findNode(placeNode, "phone");
		String area = findNode(phoneNode, "area").getTextContent();
		String number = findNode(phoneNode, "number").getTextContent();
		
		TwitterUtils.reply(capitalize(name) + ", (" + area + ") " + number + " " + shortenURL(url), 
				tweet, twitter);
	}

	private String shortenURL(String url) throws Exception {
		
		HttpGet httpget = new HttpGet(
				"http://api.bit.ly/shorten?format=xml&version=2.0.1&longUrl=" + url
						+ "&login=" + bitlyUser + "&apiKey=" + bitlyAPIKey);
		HttpResponse httpResponse = httpClient.execute(httpget);
		
		String xmlString = IOUtils.toString(httpResponse.getEntity().getContent());
		Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				new ByteArrayInputStream(xmlString.getBytes("UTF8")));
		String shortUrl = xmlDocument.getElementsByTagName("shortUrl").item(0).getTextContent();
		
		return shortUrl;
	}

	private static Node findNode(Node parentNode, String key) {
		NodeList childNodes = parentNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeName().equals(key)) {
				return childNode;
			}
		}
		return null;
	}
	
	private static String capitalize(String str) {
		String[] splitted = str.split("\\s+");
		StringBuilder strBuilder = new StringBuilder();
		for (String word : splitted) {
			char[] lowerWord = word.toLowerCase().toCharArray();
			lowerWord[0] = Character.toUpperCase(word.charAt(0));
			strBuilder.append(new String(lowerWord)).append(" ");
		}
		
		return strBuilder.toString().trim();
	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

}


