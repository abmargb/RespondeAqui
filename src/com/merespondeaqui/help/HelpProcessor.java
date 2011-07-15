package com.merespondeaqui.help;

import java.util.Set;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.merespondeaqui.Processor;

public class HelpProcessor implements Processor {

	private static final String PREFIX = "ajuda";
	private final Set<String> processorsPrefixes;

	public HelpProcessor(Set<String> processorsPrefixes) {
		this.processorsPrefixes = processorsPrefixes;
	}

	@Override
	public void process(Tweet tweet, Twitter twitter) throws TwitterException {
		
		StringBuilder strBuilder = new StringBuilder();
		for (String prefix : processorsPrefixes) {
			strBuilder.append("#").append(prefix).append(" ");
		}
		
		twitter.updateStatus("@" + tweet.getFromUser() + " " +
				"Peguntas disponíveis: " + strBuilder.toString().trim() + "");
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

}


