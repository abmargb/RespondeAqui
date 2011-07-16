package com.merespondeaqui.phone;

import java.net.URLEncoder;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.auth.BasicAuthorization;

import com.merespondeaqui.Configuration;
import com.merespondeaqui.Processor;
import com.merespondeaqui.TwitterUtils;
import com.merespondeaqui.utils.BitlyUtils;
import com.merespondeaqui.utils.Utils;
import com.merespondeaqui.utils.XMLUtils;

public class PhoneSearchProcessor implements Processor {

	private static final String PREFIX = "qualotelefone";
	private static final String FULL_PREFIX = Utils.createFullPrefix(PREFIX);
	
	private DefaultHttpClient httpClient;
	private String authHeader;
	
	public PhoneSearchProcessor() {
		String consumerKey = Configuration.getInstance().getProperty(
				"apontador.consumerkey");
		String consumerSecret = Configuration.getInstance().getProperty(
				"apontador.consumersecret");
		
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
		Document xmlDocument = XMLUtils.createDocument(xmlString);
		NodeList places = xmlDocument.getElementsByTagName("place");
		
		if (places.getLength() == 0) {
			TwitterUtils.reply("Não foi encontrado nenhum estabelecimento para a busca '" + place + "'", 
					tweet, twitter);
			return;
		}
		
		Node placeNode = places.item(0);
		
		String name = XMLUtils.findNode(placeNode, "name").getTextContent();
		String url = XMLUtils.findNode(placeNode, "main_url").getTextContent();
		
		Node phoneNode = XMLUtils.findNode(placeNode, "phone");
		String area = XMLUtils.findNode(phoneNode, "area").getTextContent();
		String number = XMLUtils.findNode(phoneNode, "number").getTextContent();
		
		String message = WordUtils.capitalizeFully(name) + ", (" + area + ") "
				+ fixAndFormatNumber(number) + " "
				+ BitlyUtils.shortenURL(url);
		
		TwitterUtils.reply(message, tweet, twitter);
	}

	private static String fixAndFormatNumber(String number) {
		char[] numberArray = number.toCharArray();
		if (numberArray[0] == '0') {
			numberArray[0] = '3';
		}
		
		StringBuilder numberBuilder = new StringBuilder();
		numberBuilder.append(Arrays.copyOfRange(numberArray, 0, 4)).append("-")
				.append(Arrays.copyOfRange(numberArray, 4, 8));
		return numberBuilder.toString();
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
}


