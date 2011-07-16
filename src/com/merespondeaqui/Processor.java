package com.merespondeaqui;

import twitter4j.Tweet;
import twitter4j.Twitter;

public interface Processor {

	public void process(Tweet tweet, Twitter twitter) throws Exception;
	
	public String getPrefix();
	
}
