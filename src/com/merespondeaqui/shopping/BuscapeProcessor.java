package com.merespondeaqui.shopping;

import java.io.IOException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import twitter4j.Tweet;
import twitter4j.Twitter;

import com.merespondeaqui.Configuration;
import com.merespondeaqui.Processor;
import com.merespondeaqui.TwitterUtils;
import com.merespondeaqui.utils.Utils;

public class BuscapeProcessor implements Processor {

	private static final String PREFIX = "qualomelhorpreco";
	private static final String FULL_PREFIX = Utils.createFullPrefix(PREFIX);
	
	private DefaultHttpClient httpClient;
	private String appId;

	public BuscapeProcessor() {
		appId = Configuration.getInstance().getProperty("buscape.applicationid");
		
		this.httpClient = new DefaultHttpClient();
	}
	
	private String doRequest(String search) {
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
			
			return shortName + " : " + price + " : " + link;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void process(Tweet tweet, Twitter twitter) throws Exception {
		String text = tweet.getText();
		
		String[] splitText = text.split("\\s+");
		String product = text.substring(FULL_PREFIX.length() + splitText[2].length() + 2);
		
		String result = doRequest(product);
		
		TwitterUtils.reply(result, tweet, twitter);
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
}
