package com.merespondeaqui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetCrawler implements Runnable {

	private static final String MERESPONDEAQUI = "@merespondeaqui";
	private static final String LASTTWEET_FILE = "lasttweet";
	
	private final BlockingQueue<Tweet> tweetQueue;
	private final Twitter twitter;
	private Long lastTweet;

	public TweetCrawler(Twitter twitter, BlockingQueue<Tweet> tweetQueue) {
		this.tweetQueue = tweetQueue;
		this.twitter = twitter;
		this.lastTweet = readLastTweetId();
	}
	
	@Override
	public void run() {
	
		while (true) {
			
			while (true) {
				
				Query query = new Query();
				query.setQuery(MERESPONDEAQUI);
				query.setRpp(100);
				
				if (lastTweet != null) {
					query.setSinceId(lastTweet);
				}
				
				QueryResult queryResult = null;
				try {
					queryResult = twitter.search(query);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				
				if (queryResult != null) {
					
					List<Tweet> tweets = queryResult.getTweets();
					if (tweets.isEmpty()) {
						break;
					}
					
					for (Tweet tweet : tweets) {
						tweetQueue.add(tweet);
					}
					
					lastTweet = queryResult.getMaxId();
				}
				

				// Avoid rate limit errors
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (lastTweet != null) {
				writeLastTweetId();
			}
			
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeLastTweetId() {
		try {
			FileWriter fileWriter = new FileWriter(LASTTWEET_FILE);
			fileWriter.write(lastTweet.toString());
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
