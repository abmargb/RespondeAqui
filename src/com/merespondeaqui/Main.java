package com.merespondeaqui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.merespondeaqui.calculator.CalculatorProcessor;
import com.merespondeaqui.distance.DistanceProcessor;
import com.merespondeaqui.placar.PlacarProcessor;
import com.merespondeaqui.weather.WeatherProcessor;

public class Main {

	private static final String LASTTWEET_FILE = "lasttweet";
	private static final String PROPERTIES_FILE = "conf.properties";

	public static void main(String[] args) {
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(PROPERTIES_FILE));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(properties.getProperty("consumerkey"));
		configurationBuilder.setOAuthConsumerSecret(properties.getProperty("consumersecret"));
		configurationBuilder.setOAuthAccessToken(properties.getProperty("accesstoken"));
		configurationBuilder.setOAuthAccessTokenSecret(properties.getProperty("accesstokensecret"));
		
		final Twitter twitter = new TwitterFactory(configurationBuilder.build()).getInstance();
		Map<String, Processor> processors = new HashMap<String, Processor>();
		addProcessor(processors, new DistanceProcessor());
		addProcessor(processors, new WeatherProcessor());
		addProcessor(processors, new CalculatorProcessor());
		addProcessor(processors, new PlacarProcessor());
		
		Long lastTweet = readLastTweetId();
		
		while (true) {
			
			Query query = new Query();
			query.setQuery("@merespondeaqui");
			
			if (lastTweet != null) {
				query.setSinceId(lastTweet + 1);
			}
			
			QueryResult queryResult = null;
			try {
				queryResult = twitter.search(query);
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			
			if (queryResult != null) {
				List<Tweet> tweets = queryResult.getTweets();
				
				for (Tweet tweet : tweets) {
					
					lastTweet = tweet.getId();
					
					String[] split = tweet.getText().split("\\s+");
					if (split.length < 2) {
						continue;
					}
					
					String processorPrefix = split[1];
					Processor processor = processors.get(processorPrefix.substring(1));
					
					if (processor == null) {
						continue;
					}
					
					try {
						processor.process(tweet, twitter);
					} catch (TwitterException e) {
						e.printStackTrace();
					}
				}
			}
			
			if (lastTweet != null) {
				try {
					FileWriter fileWriter = new FileWriter(LASTTWEET_FILE);
					fileWriter.write(lastTweet.toString());
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				Thread.sleep(120000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
	}

	private static void addProcessor(Map<String, Processor> processors,
			Processor processor) {
		processors.put(processor.getPrefix(), processor);
	}

	private static Long readLastTweetId() {
		try {
			Scanner scanner = new Scanner(new File(LASTTWEET_FILE));
			return scanner.nextLong();
		} catch (FileNotFoundException e) {
			return null;
		}
	}
}
