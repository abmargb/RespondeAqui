package com.merespondeaqui.weather;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import twitter4j.Tweet;
import twitter4j.Twitter;

import com.merespondeaqui.Processor;
import com.merespondeaqui.TwitterUtils;
import com.merespondeaqui.utils.Utils;
import com.merespondeaqui.utils.XMLUtils;

public class WeatherProcessor implements Processor {

	private static final String PREFIX = "comoestaoclima";
	private static final String FULL_PREFIX = Utils.createFullPrefix(PREFIX);
	private static final String EM = "em";
	
	@Override
	public void process(Tweet tweet, Twitter twitter) {
		
		String text = tweet.getText().toLowerCase();
		
		String fromStr = text.substring(FULL_PREFIX.length() + EM.length() + 2);
		
		try {
			InputStream stream = new URL(
					"http://www.google.co.uk/ig/api?weather=" + URLEncoder.encode(fromStr, "UTF-8") + "&hl=pt-br"
			).openStream();
			
			Document xmlDocument = XMLUtils.createDocument(IOUtils.toString(stream));
			
			Node currentCondition = xmlDocument.getElementsByTagName("current_conditions").item(0);
			
			Node condition = XMLUtils.findNode(currentCondition, "condition");
			String conditionStr = condition.getAttributes().getNamedItem("data").getNodeValue();
			
			Node temp_c = XMLUtils.findNode(currentCondition, "temp_c");
			String tempCStr = temp_c.getAttributes().getNamedItem("data").getNodeValue();
			
			Node humidity = XMLUtils.findNode(currentCondition, "humidity");
			String humidityStr = humidity.getAttributes().getNamedItem("data").getNodeValue();
			
			String message = "Clima: " + conditionStr + ", Temperatura: " + tempCStr + "ºC e " + humidityStr;
			TwitterUtils.reply(message, tweet, twitter);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

}
