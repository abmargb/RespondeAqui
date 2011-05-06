package com.merespondeaqui;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public interface Processor {

	public void process(Tweet tweet, Twitter twitter) throws TwitterException;
	
	public String getPrefix();
	
}
