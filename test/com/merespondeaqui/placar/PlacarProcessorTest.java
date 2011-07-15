package com.merespondeaqui.placar;

import org.easymock.EasyMock;
import org.junit.Test;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.merespondeaqui.TweetImpl;

public class PlacarProcessorTest {

	@Test
	public void testBrazilGame() throws TwitterException {

		Twitter twitter = EasyMock.createMock(Twitter.class);
		String response = "@user Brasil 4 - 2 Equador, Finalizado";
		
		EasyMock.expect(twitter.updateStatus(response)).andReturn(null);
		EasyMock.replay(twitter);
		
		PlacarProcessor placarProcessor = new PlacarProcessor();
		Tweet tweet = new TweetImpl(
				"@merespondeaqui #quantoestaojogo do Brasil", "user");
		placarProcessor.process(tweet, twitter);
		
		EasyMock.verify(twitter);
		
	}

	
}
