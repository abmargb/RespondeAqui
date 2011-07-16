package com.merespondeaqui.places;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.Mockito;

import twitter4j.StatusUpdate;
import twitter4j.Tweet;
import twitter4j.Twitter;

import com.merespondeaqui.TweetImpl;

public class PlaceSearchProcessorTest {

	@Test
	public void testPlaceSearch() throws Exception {

		Twitter twitter = Mockito.mock(Twitter.class);
		
		PlaceSearchProcessor placesProcessor = new PlaceSearchProcessor();
		Tweet tweet = new TweetImpl(
				"@merespondeaqui #ondetem restaurante perto do Cuscuz, Campina Grande", "user");
		placesProcessor.process(tweet, twitter);
		
		String response = "@user Bar Do Cuscuz, (83) 3322-4232 http://bit.ly/";
		Mockito.verify(twitter).updateStatus(createStatusMatcher(response));
	}

	private static StatusUpdate createStatusMatcher(final String status) {
		return Mockito.argThat(new BaseMatcher<StatusUpdate>() {

			@Override
			public boolean matches(Object item) {
				StatusUpdate statusUpdate = (StatusUpdate) item;
				return statusUpdate.getStatus().startsWith(status)
						&& statusUpdate.getInReplyToStatusId() == 0;
			}

			@Override
			public void describeTo(Description description) {
				
			}
		});
	}
	
}
