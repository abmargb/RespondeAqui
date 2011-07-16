package com.merespondeaqui;

import twitter4j.StatusUpdate;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterUtils {

	public static void reply(String message, Tweet originalTweet,
			Twitter twitter) throws TwitterException {
		
		twitter.updateStatus(new StatusUpdate("@" + originalTweet.getFromUser() + " " + message)
				.inReplyToStatusId(originalTweet.getId()));

	}
	
}
