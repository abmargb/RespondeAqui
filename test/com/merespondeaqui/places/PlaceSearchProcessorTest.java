package com.merespondeaqui.places;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.Mockito;

import twitter4j.GeoLocation;
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
				"@merespondeaqui #ondetem padaria perto do Bar do Cuscuz", "user");
		placesProcessor.process(tweet, twitter);
		
		String response = "@user Panificadora Pastiery, Rua Miguel Couto, 139, Centro http://bit.ly/";
		Mockito.verify(twitter).updateStatus(createStatusMatcher(response));
	}
	
	@Test
	public void testPlaceNoCategories() throws Exception {

		Twitter twitter = Mockito.mock(Twitter.class);
		
		PlaceSearchProcessor placesProcessor = new PlaceSearchProcessor();
		Tweet tweet = new TweetImpl(
				"@merespondeaqui #ondetem blablabla perto do Bar do Cuscuz", "user");
		placesProcessor.process(tweet, twitter);
		
		String response = "@user Não foi encontrada nenhuma categoria de estabelecimento para a busca 'blablabla'";
		Mockito.verify(twitter).updateStatus(createStatusMatcher(response));
	}
	
	@Test
	public void testPlaceNoPlace() throws Exception {

		Twitter twitter = Mockito.mock(Twitter.class);
		
		PlaceSearchProcessor placesProcessor = new PlaceSearchProcessor();
		Tweet tweet = new TweetImpl(
				"@merespondeaqui #ondetem hospitais perto do OAihlaksjAOI", "user");
		placesProcessor.process(tweet, twitter);
		
		String response = "@user Não foi encontrado nenhum estabelecimento próximo a 'oaihlaksjaoi'";
		Mockito.verify(twitter).updateStatus(createStatusMatcher(response));
	}

	@Test
	public void testPlaceMyPlaceNoGPS() throws Exception {

		Twitter twitter = Mockito.mock(Twitter.class);
		
		PlaceSearchProcessor placesProcessor = new PlaceSearchProcessor();
		Tweet tweet = new TweetImpl(
				"@merespondeaqui #ondetem hospitais perto daqui", "user");
		placesProcessor.process(tweet, twitter);
		
		String response = "@user Desculpe, mas não sei onde você está. Seu dispositivo tem GPS?";
		Mockito.verify(twitter).updateStatus(createStatusMatcher(response));
	}
	
	@Test
	public void testPlaceMyPlaceWithGPS() throws Exception {

		Twitter twitter = Mockito.mock(Twitter.class);
		
		PlaceSearchProcessor placesProcessor = new PlaceSearchProcessor();
		TweetImpl tweet = new TweetImpl(
				"@merespondeaqui #ondetem padaria perto daqui", "user");
		tweet.setGeoLocation(new GeoLocation(-7.22502, -35.88106));
		placesProcessor.process(tweet, twitter);
		
		String response = "@user Panificadora Pastiery, Rua Miguel Couto, 139, Centro http://bit.ly/";
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
