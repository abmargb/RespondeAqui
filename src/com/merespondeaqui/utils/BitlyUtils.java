package com.merespondeaqui.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;

import com.merespondeaqui.Configuration;

public class BitlyUtils {

	public static String shortenURL(String url) throws Exception {
		
		Configuration properties = Configuration.getInstance();
		
		String bitlyUser = properties.getProperty("bitly.username");
		String bitlyAPIKey = properties.getProperty("bitly.apikey");
		
		HttpGet httpget = new HttpGet(
				"http://api.bit.ly/shorten?format=xml&version=2.0.1&longUrl=" + url
						+ "&login=" + bitlyUser + "&apiKey=" + bitlyAPIKey);
		HttpResponse httpResponse = new DefaultHttpClient().execute(httpget);
		
		String xmlString = IOUtils.toString(httpResponse.getEntity().getContent());
		Document xmlDocument = XMLUtils.createDocument(xmlString);
		String shortUrl = xmlDocument.getElementsByTagName("shortUrl").item(0).getTextContent();
		
		return shortUrl;
	}

}
