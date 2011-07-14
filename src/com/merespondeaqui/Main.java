package com.merespondeaqui;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Main {

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
		
		BlockingQueue<Tweet> tweetQueue = new LinkedBlockingQueue<Tweet>();
		new Thread(new TweetCrawler(twitter, tweetQueue)).start();
		new Thread(new TweetConsumer(twitter, tweetQueue)).start();
			
	}
}
