package com.merespondeaqui.distance;

import java.text.DecimalFormat;

import twitter4j.Tweet;
import twitter4j.Twitter;

import com.merespondeaqui.Processor;
import com.merespondeaqui.TwitterUtils;
import com.merespondeaqui.utils.GeoUtils;
import com.merespondeaqui.utils.Utils;

public class DistanceProcessor implements Processor {

	private static final String PARA = "para";
	private static final String DE = "de";
	private static final String PREFIX = "qualadistancia";
	private static final String FULL_PREFIX = Utils.createFullPrefix(PREFIX);
	
	private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public void process(Tweet tweet, Twitter twitter) throws Exception {
		
		String text = tweet.getText().toLowerCase();
		
		int indexPara = text.indexOf(PARA);
		
		String fromStr = text.substring(FULL_PREFIX.length() + DE.length() + 2, indexPara);
		String toStr = text.substring(indexPara + PARA.length() + 1, text.length());
		
		TwitterUtils.reply(
				"São " + FORMAT.format(GeoUtils.distance(fromStr, toStr)) + " Km", 
				tweet, twitter);
			
	}

}
