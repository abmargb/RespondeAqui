package com.merespondeaqui.shopping;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Properties;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.auth.BasicAuthorization;

import com.merespondeaqui.Processor;
import com.merespondeaqui.Utils;

public class BuscapeProcessor implements Processor {

	private static final String PREFIX = "qualomelhorpreco";
	private static final String FULL_PREFIX = Utils.createFullPrefix(PREFIX);
	
	private String bitlyUser;
	private String bitlyAPIKey;
	private DefaultHttpClient httpClient;
	private String appId;

	public BuscapeProcessor(Properties properties) {
		appId = properties.getProperty("buscape.applicationid");
		
		this.bitlyUser = properties.getProperty("bitly.username");
		this.bitlyAPIKey = properties.getProperty("bitly.apikey");
		
		this.httpClient = new DefaultHttpClient();
	}

	private void doRequest(String search) {
		HttpGet httpget = new HttpGet("http://sandbox.buscape.com/service/findProductList/" + appId + "/?keyword=" + search +
				"&format=json&sort=price");
		try {
			HttpResponse response = httpClient.execute(httpget);
			
			JSONObject jsonObject = JSONObject.fromObject(IOUtils.toString(
					response.getEntity().getContent()));
			
			System.out.println(jsonObject.toString());
			
			JSONArray products = jsonObject.getJSONArray("product");
			JSONObject product = products.getJSONObject(0);
			product = product.getJSONObject("product");
			JSONArray links = product.getJSONArray("links");
			
			String shortName = product.getString("productshortname");
			String price = product.getString("pricemin");
			String link = null;
			for (int i = 0; i < links.size(); i++) {
				JSONObject obj = links.getJSONObject(i);
				obj = obj.getJSONObject("link");
				if ("product".equals(obj.getString("type"))) {
					link = obj.getString("url");
				}
			}
			
			
			System.out.println("Name: " + shortName);
			System.out.println("Price: " + price);
			System.out.println("Link: " + link);
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void process(Tweet tweet, Twitter twitter) throws Exception {
		String text = tweet.getText();
		
		String[] splitText = text.split("\\s+");
		String place = text.substring(FULL_PREFIX.length() + splitText[2].length() + 2);
		
		HttpGet httpget = new HttpGet("http://api.apontador.com.br/v1/search/places?q=" + URLEncoder.encode(place, "UTF-8"));
		
		HttpResponse httpResponse = httpClient.execute(httpget);
		
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
	public static void main(String[] args) {
		Properties p = new Properties();
		p.setProperty("buscape.applicationid", "564771466d477a4458664d3d");
		
		BuscapeProcessor bp = new BuscapeProcessor(p);
		bp.doRequest("n900");
	}

}
