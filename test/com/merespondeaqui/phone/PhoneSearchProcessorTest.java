package com.merespondeaqui.phone;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.easymock.EasyMock;
import org.junit.Test;

import twitter4j.StatusUpdate;
import twitter4j.Tweet;
import twitter4j.Twitter;

import com.merespondeaqui.TweetImpl;

public class PhoneSearchProcessorTest {

	@Test
	public void testPhoneSearch() throws Exception {

		Twitter twitter = EasyMock.createMock(Twitter.class);
		String response = "@user Forno De Pizza, (83) 33420606";
		StatusUpdate latestStatus = new StatusUpdate(response);
		latestStatus.setInReplyToStatusId(0);
		
		EasyMock.expect(twitter.updateStatus(latestStatus)).andReturn(null);
		EasyMock.replay(twitter);
		
		PhoneSearchProcessor phoneProcessor = new PhoneSearchProcessor(loadProperties());
		Tweet tweet = new TweetImpl(
				"@merespondeaqui #qualotelefone da Forno de Pizza, Campina Grande", "user");
		phoneProcessor.process(tweet, twitter);
		
		EasyMock.verify(twitter);
		
	}

	private static Properties loadProperties() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("conf.properties"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return properties;
	}
	
}
