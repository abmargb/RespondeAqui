package com.merespondeaqui.help;

import org.junit.Test;
import org.mockito.Mockito;

import twitter4j.StatusUpdate;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.merespondeaqui.TweetImpl;

public class HelpProcessorTest {

	@Test
	public void testHelp() throws TwitterException {

		Twitter twitter = Mockito.mock(Twitter.class);
		
		HelpProcessor helpProcessor = new HelpProcessor();
		Tweet tweet = new TweetImpl(
				"@merespondeaqui #ajuda", "user");
		helpProcessor.process(tweet, twitter);
		
		String response = "@user Ajuda do @merespondeaqui: http://bit.ly/qZXKMI";
		StatusUpdate latestStatus = new StatusUpdate(response);
		latestStatus.setInReplyToStatusId(0);
		Mockito.verify(twitter).updateStatus(latestStatus);
		
	}

	
}
