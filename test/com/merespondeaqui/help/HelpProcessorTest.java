package com.merespondeaqui.help;

import org.easymock.EasyMock;
import org.junit.Test;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.merespondeaqui.TweetImpl;

public class HelpProcessorTest {

	@Test
	public void testBrazilGame() throws TwitterException {

		Twitter twitter = EasyMock.createMock(Twitter.class);
		String response = "@user Ajuda do @merespondeaqui: http://bit.ly/qZXKMI";
		
		EasyMock.expect(twitter.updateStatus(response)).andReturn(null);
		EasyMock.replay(twitter);
		
		HelpProcessor helpProcessor = new HelpProcessor();
		Tweet tweet = new TweetImpl(
				"@merespondeaqui #ajuda", "user");
		helpProcessor.process(tweet, twitter);
		
		EasyMock.verify(twitter);
		
	}

	
}
