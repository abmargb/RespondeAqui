package com.merespondeaqui.help;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.merespondeaqui.Processor;
import com.merespondeaqui.TwitterUtils;

public class HelpProcessor implements Processor {

	private static final String PREFIX = "ajuda";
	private static final String HELP_URL = "http://bit.ly/qZXKMI";
	
	@Override
	public void process(Tweet tweet, Twitter twitter) throws TwitterException {
		TwitterUtils.reply(
				"Ajuda do @merespondeaqui: " + HELP_URL, 
				tweet, twitter);
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

}


