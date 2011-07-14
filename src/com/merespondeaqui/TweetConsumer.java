package com.merespondeaqui;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.merespondeaqui.calculator.CalculatorProcessor;
import com.merespondeaqui.distance.DistanceProcessor;
import com.merespondeaqui.placar.PlacarProcessor;
import com.merespondeaqui.weather.WeatherProcessor;

public class TweetConsumer implements Runnable {

	private final Map<String, Processor> processors;
	private final BlockingQueue<Tweet> tweetQueue;
	private final Twitter twitter;

	public TweetConsumer(Twitter twitter, BlockingQueue<Tweet> tweetQueue) {
		this.twitter = twitter;
		this.tweetQueue = tweetQueue;
		
		this.processors = new HashMap<String, Processor>();
		addProcessor(new DistanceProcessor());
		addProcessor(new WeatherProcessor());
		addProcessor(new CalculatorProcessor());
		addProcessor(new PlacarProcessor());
	}
	
	@Override
	public void run() {
		while (true) {
			Tweet tweet = null;
			try {
				tweet = tweetQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			
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
	
	private void addProcessor(Processor processor) {
		processors.put(processor.getPrefix(), processor);
	}
	
}
