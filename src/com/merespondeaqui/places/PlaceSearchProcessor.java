package com.merespondeaqui.places;

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
import com.merespondeaqui.utils.GeoUtils;
import com.merespondeaqui.utils.Utils;
import com.merespondeaqui.utils.XMLUtils;

public class PlaceSearchProcessor implements Processor {

	private static final String PERTO = "perto";
	private static final String PREFIX = "ondetem";
	private static final String FULL_PREFIX = Utils.createFullPrefix(PREFIX);
	
	private DefaultHttpClient httpClient;
	private String authHeader;
	
	public PlaceSearchProcessor() {
		Configuration properties = Configuration.getInstance();
		String consumerKey = properties.getProperty("apontador.consumerkey");
		String consumerSecret = properties.getProperty("apontador.consumersecret");
		
		this.authHeader = new BasicAuthorization(consumerKey, consumerSecret)
				.getAuthorizationHeader(null);
		this.httpClient = new DefaultHttpClient();
	}

	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public void process(Tweet tweet, Twitter twitter) throws Exception {
		
		String text = tweet.getText().toLowerCase();
		
		int indexPara = text.indexOf(PERTO);
		
		String categoriaStr = text.substring(FULL_PREFIX.length() + 1, indexPara);
		int indexOfNextSpace = text.indexOf(" ", indexPara + PERTO.length() + 1);
		
		String placeStr = text.substring(indexOfNextSpace + 1, text.length());
		
		double[] boundingBox = GeoUtils.boundingBox(placeStr, 1.);
		
		findPlaces(categoriaStr, boundingBox, tweet, twitter);
			
	}
	
	private void findPlaces(String category, double[] boundingBox, Tweet tweet, Twitter twitter) throws Exception {

		HttpGet httpget = new HttpGet(
				"http://api.apontador.com.br/v1/search/places/bybox?se_lat="
						+ boundingBox[0] + "&se_lng=" + boundingBox[1]
						+ "&nw_lat=" + boundingBox[2] + "&nw_lng="
						+ boundingBox[3]);
		httpget.setHeader("Authorization", authHeader);
		
		HttpResponse httpResponse = httpClient.execute(httpget);
		
		String xmlString = IOUtils.toString(httpResponse.getEntity().getContent());
		Document xmlDocument = XMLUtils.createDocument(xmlString);
		
		NodeList places = xmlDocument.getElementsByTagName("place");
		
		if (places.getLength() == 0) {
			TwitterUtils.reply("Não foi encontrado nenhum estabelecimento para a busca '" + category + "'", 
					tweet, twitter);
			return;
		}
		
		Node placeNode = places.item(0);
		
		String name = XMLUtils.findNode(placeNode, "name").getTextContent();
		String url = XMLUtils.findNode(placeNode, "main_url").getTextContent();
		
		Node addressNode = XMLUtils.findNode(placeNode, "address");
		String street = XMLUtils.findNode(addressNode, "street").getTextContent();
		String number = XMLUtils.findNode(addressNode, "number").getTextContent();
		String district = XMLUtils.findNode(addressNode, "district").getTextContent();
		
		String message = WordUtils.capitalizeFully(name) + ", " + street + ", " + number + ", "
				+ district + " "
				+ BitlyUtils.shortenURL(url);

		TwitterUtils.reply(message, tweet, twitter);
	}
	

}
