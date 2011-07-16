package com.merespondeaqui.placar;

import org.junit.Test;
import org.mockito.Mockito;

import twitter4j.StatusUpdate;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.merespondeaqui.TweetImpl;

public class PlacarProcessorTest {

	/**
	 * For debugging purposes only
	 * @throws TwitterException
	 */
	@Test
	public void testBrazilGame() throws TwitterException {

		Twitter twitter = Mockito.mock(Twitter.class);
		
		PlacarProcessor placarProcessor = new PlacarProcessor();
		Tweet tweet = new TweetImpl(
				"@merespondeaqui #quantoestaojogo do Brasil", "user");
		placarProcessor.process(tweet, twitter);
		
		String response = "@user Brasil 4 - 2 Equador, Finalizado";
		StatusUpdate latestStatus = new StatusUpdate(response);
		latestStatus.setInReplyToStatusId(0);
		
	}

	
}
