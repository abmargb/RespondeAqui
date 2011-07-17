package com.merespondeaqui.places;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import twitter4j.GeoLocation;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.auth.BasicAuthorization;

import com.merespondeaqui.Configuration;
import com.merespondeaqui.Processor;
import com.merespondeaqui.TwitterUtils;
import com.merespondeaqui.utils.BitlyUtils;
import com.merespondeaqui.utils.GeoUtils;
import com.merespondeaqui.utils.GeoUtils.LatLng;
import com.merespondeaqui.utils.Utils;
import com.merespondeaqui.utils.XMLUtils;

public class PlaceSearchProcessor implements Processor {

	private static final String PERTO = "perto";
	private static final String DAQUI = "daqui";
	private static final String PREFIX = "ondetem";
	private static final String FULL_PREFIX = Utils.createFullPrefix(PREFIX);
	
	private DefaultHttpClient httpClient;
	private String authHeader;
	private HashMap<String, Integer> categories;
	
	public PlaceSearchProcessor() {
		Configuration properties = Configuration.getInstance();
		String consumerKey = properties.getProperty("apontador.consumerkey");
		String consumerSecret = properties.getProperty("apontador.consumersecret");
		
		this.authHeader = new BasicAuthorization(consumerKey, consumerSecret)
				.getAuthorizationHeader(null);
		this.httpClient = new DefaultHttpClient();
		
		try {
			loadCategories();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public void process(Tweet tweet, Twitter twitter) throws Exception {
		
		String text = tweet.getText().toLowerCase();
		
		int indexPerto = text.indexOf(PERTO);
		int indexFimPerto = indexPerto + PERTO.length() + 1;
		
		String categoriaStr = text.substring(FULL_PREFIX.length() + 1, indexPerto - 1);
		int indexOfNextSpace = text.indexOf(" ", indexFimPerto);
		
		LatLng placeLatLng = null;
		String placeStr = null;
		
		boolean temDaqui = false;
		
		try {
			temDaqui = text.substring(indexFimPerto, indexFimPerto + DAQUI.length()).equals(DAQUI);
		} catch (Exception e) {}
		
		if (temDaqui) {
			
			GeoLocation geoLocation = tweet.getGeoLocation();
			if (geoLocation == null) {
				TwitterUtils.reply(
						"Desculpe, mas não sei onde você está. Seu dispositivo tem GPS?", tweet, twitter);
				return;
			}
			
			placeStr = DAQUI;
			placeLatLng = new LatLng(geoLocation.getLatitude(),
					geoLocation.getLongitude());
		} else {
			placeStr = text.substring(indexOfNextSpace + 1, text.length());
			placeLatLng = GeoUtils.getLatLng(placeStr);
		}
		
		double[] boundingBox = GeoUtils.boundingBox(placeLatLng, 0.5);
		
		findPlaces(categoriaStr, placeStr, placeLatLng, boundingBox, tweet, twitter);
			
	}
	
	private void findPlaces(String category, String placeStr,
			LatLng placeLatLng, double[] boundingBox, Tweet tweet,
			Twitter twitter) throws Exception {

		Integer categoryId = findCategoryId(category);
		
		if (categoryId == null) {
			TwitterUtils.reply(
					"Não foi encontrada nenhuma categoria de estabelecimento para a busca '"
							+ category + "'", tweet, twitter);
			return;
		}
		
		HttpGet httpget = new HttpGet(
				"http://api.apontador.com.br/v1/search/places/bybox?se_lat="
						+ boundingBox[0] + "&se_lng=" + boundingBox[1]
						+ "&nw_lat=" + boundingBox[2] + "&nw_lng="
						+ boundingBox[3] + "&category_id=" + categoryId);
		httpget.setHeader("Authorization", authHeader);
		
		HttpResponse httpResponse = httpClient.execute(httpget);
		
		String xmlString = IOUtils.toString(httpResponse.getEntity().getContent());
		Document xmlDocument = XMLUtils.createDocument(xmlString);
		
		NodeList places = xmlDocument.getElementsByTagName("place");
		
		List<PlaceNode> placeNodes = new LinkedList<PlaceSearchProcessor.PlaceNode>();
		
		for (int i = 0; i < places.getLength(); i++) {
			
			Node placeNode = places.item(i);
			
			String placeId = XMLUtils.findNode(placeNode, "id").getTextContent();
			
			if (placeLatLng.getPlaceId() != null
					&& placeLatLng.getPlaceId().equals(placeId)) {
				continue;
			}
			
			Node pointNode = XMLUtils.findNode(placeNode, "point");
			double lat = Double.valueOf(XMLUtils.findNode(pointNode, "lat").getTextContent());
			double lng = Double.valueOf(XMLUtils.findNode(pointNode, "lng").getTextContent());
			
			placeNodes.add(new PlaceNode(GeoUtils.distance(placeLatLng.getLat(),
					placeLatLng.getLng(), lat, lng), placeNode));
			
		}
		
		if (placeNodes.isEmpty()) {
			TwitterUtils.reply("Não foi encontrado nenhum estabelecimento próximo a '" + placeStr + "'", 
					tweet, twitter);
			return;
		}
		
		Collections.sort(placeNodes, new Comparator<PlaceNode>() {
			@Override
			public int compare(PlaceNode o1, PlaceNode o2) {
				return o1.distance.compareTo(o2.distance);
			}
		});
		
		Node placeNode = placeNodes.iterator().next().node;
		
		String name = XMLUtils.findNode(placeNode, "name").getTextContent();
		String url = XMLUtils.findNode(placeNode, "main_url").getTextContent();
		
		Node addressNode = XMLUtils.findNode(placeNode, "address");
		String street = XMLUtils.findNode(addressNode, "street").getTextContent();
		String number = XMLUtils.findNode(addressNode, "number").getTextContent();
		String district = XMLUtils.findNode(addressNode, "district").getTextContent();
		
		String message = WordUtils.capitalizeFully(name) + ", " + street
				+ ", " + number + ", " + district + " "
				+ BitlyUtils.shortenURL(url);
		
		TwitterUtils.reply(message, tweet, twitter);
	}
	
	private static class PlaceNode {
		Double distance;
		Node node;
		
		public PlaceNode(Double distance, Node node) {
			this.distance = distance;
			this.node = node;
		}
	}
	
	private Integer findCategoryId(String category) {
		
		double minSimilarity = Double.MAX_VALUE;
		Integer categoryId = 0;
		
		for (Entry<String, Integer> categoryEntry : categories.entrySet()) {
			String name = categoryEntry.getKey();
			
			double similarity = 0;
			if (!name.contains(category)) {
				int size = Math.max(category.length(), name.length());
				similarity = (double) StringUtils.getLevenshteinDistance(name,
						category) / (double) size;
			}
			
			if (similarity < minSimilarity) {
				categoryId = categoryEntry.getValue();
				minSimilarity = similarity;
			}
			
			if (minSimilarity < 0.1) {
				return categoryId;
			}
		}
		
		if (minSimilarity > 0.5) {
			return null;
		}
		
		return categoryId;
	}


	private void loadCategories() throws Exception {
		
		HttpGet httpget = new HttpGet(
				"http://api.apontador.com.br/v1/categories");
		httpget.setHeader("Authorization", authHeader);
		
		HttpResponse httpResponse = httpClient.execute(httpget);
		
		String xmlString = IOUtils.toString(httpResponse.getEntity().getContent());
		Document xmlDocument = XMLUtils.createDocument(xmlString);
		
		NodeList categoriesList = xmlDocument.getElementsByTagName("category");
		
		this.categories = new HashMap<String, Integer>();
		
		for (int i = 0; i < categoriesList.getLength(); i++) {
			Node categoryNode = categoriesList.item(i);
			Integer id = Integer.valueOf(XMLUtils.findNode(categoryNode, "id").getTextContent());
			String name = XMLUtils.findNode(categoryNode, "name").getTextContent();
			
			categories.put(name, id);
		}
	}
}
