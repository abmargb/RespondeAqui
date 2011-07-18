package com.merespondeaqui.shopping;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.Mockito;

import twitter4j.StatusUpdate;
import twitter4j.Tweet;
import twitter4j.Twitter;

import com.merespondeaqui.TweetImpl;

public class BuscapeProcessorTest {

	@Test
	public void testPhoneSearch() throws Exception {

		Twitter twitter = Mockito.mock(Twitter.class);
		
		BuscapeProcessor buscapeProcessor = new BuscapeProcessor();
		Tweet tweet = new TweetImpl(
				"@merespondeaqui #ondecomprar N900", "user");
		buscapeProcessor.process(tweet, twitter);
		
		String response = "@user";
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
